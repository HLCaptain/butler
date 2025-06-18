package illyan.butler.shared.llm

import com.aallam.openai.api.audio.AudioResponseFormat
import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.audio.SpeechResponseFormat
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.audio.Voice
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.chat.ContentPart
import com.aallam.openai.api.chat.ImagePart
import com.aallam.openai.api.chat.ListContent
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Capability
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.MessageDto
import illyan.butler.shared.model.chat.ResourceDto
import illyan.butler.shared.model.chat.SenderType
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.io.asSource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val CONTENT_TYPE_IMAGE = "image"
private const val CONTENT_TYPE_AUDIO = "audio"
private const val RESOURCE_TYPE_AUDIO_MP3 = "audio/mp3"

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
class LlmService(
    private val coroutineScopeIO: CoroutineScope,
    private val getResource: suspend (resourceId: Uuid, ownerId: Uuid) -> ResourceDto,
    private val createResource: suspend (chatId: Uuid, senderId: SenderType.Ai, resource: ResourceDto) -> ResourceDto,
    private val upsertMessage: suspend (message: MessageDto) -> MessageDto,
    private val getOpenAIClient: suspend (endpoint: String) -> OpenAI,
    private val upsertChat: suspend (chat: ChatDto) -> ChatDto,
    private val errorInMessageResponse: suspend (message: MessageDto?) -> Unit,
    private val removeMessage: suspend (message: MessageDto) -> Unit,
) {
    @OptIn(ExperimentalTime::class)
    suspend fun answerChat(
        chat: ChatDto,
        chatMessages: List<MessageDto>,
        previousChats: List<ChatDto> = emptyList(),
        regenerateMessage: MessageDto? = null
    ) {
        Napier.v("Answering chat ${chat.id}")

        val (initialMessages, lastMessage, isRegenerating) = prepareConversation(chatMessages, regenerateMessage)
        if (initialMessages.isEmpty()) {
            Napier.v("No messages in chat, skipping")
            return
        }

        if (lastMessage.senderId != chat.ownerId.toString() && !isRegenerating) {
            Napier.v("Last message is not from the user, skipping answer generation.")
            updateChatNameAndSummary(chat, initialMessages, emptyList())
            return
        }

        val resources = initialMessages.map { message -> message.resourceIds.map { getResource(it, chat.ownerId) } }.flatten()
        val conversation = initialMessages.toConversation(chat.ownerId.toString(), resources)

        val updatedMessages = when {
            lastMessage.resourceIds.isEmpty() -> handleTextMessage(chat, conversation, previousChats, regenerateMessage)
            else -> handleResourceMessage(chat, lastMessage, conversation, previousChats, regenerateMessage, resources)
        }

        updateChatNameAndSummary(chat, initialMessages + updatedMessages, resources)
    }

    private suspend fun prepareConversation(
        chatMessages: List<MessageDto>,
        regenerateMessage: MessageDto?
    ): Triple<List<MessageDto>, MessageDto, Boolean> {
        var messages = chatMessages.sortedBy { it.time }
        val isRegenerating = regenerateMessage != null
        val lastMessage = if (isRegenerating) {
            messages.first { it.id == regenerateMessage!!.id }
        } else {
            messages.last()
        }

        if (lastMessage.senderId != regenerateMessage?.senderId && lastMessage.content.isNullOrBlank()) {
            Napier.v("Last message from AI is blank, removing it.")
            removeMessage(lastMessage)
            messages = messages.filter { it.id != lastMessage.id }
        }
        return Triple(messages, messages.last(), isRegenerating)
    }

    private suspend fun handleTextMessage(
        chat: ChatDto,
        conversation: List<ChatMessage>,
        previousChats: List<ChatDto>,
        regenerateMessage: MessageDto?
    ): List<MessageDto> {
        Napier.v("Handling text message")
        val answerFlow = answerChatWithTextAndContextStream(chat, conversation, previousChats)
        var updatedAnswer: MessageDto? = null

        val firstAnswer = try {
            answerFlow.firstOrNull { !it.isNullOrBlank() }
        } catch (e: Exception) {
            Napier.e("Error getting first answer", e)
            errorInMessageResponse(null)
            return emptyList()
        }

        if (firstAnswer == null) {
            Napier.v("Got empty answer, finishing.")
            return emptyList()
        }

        updatedAnswer = toNewMessage(regenerateMessage, chat, firstAnswer).let { upsertMessage(it) }
        val initialMessage = updatedAnswer

        coroutineScopeIO.launch {
            answerFlow.catch {
                Napier.e("Error in chat completion stream", it)
                updatedAnswer?.let { msg -> errorInMessageResponse(msg) }
                cancel()
            }.collect { answer ->
                if (answer == null) {
                    Napier.v("Completion ended for message: ${updatedAnswer?.id}")
                    cancel()
                    return@collect
                }
                updatedAnswer = toNewMessage(updatedAnswer, chat, answer).let { upsertMessage(it) }
            }
        }
        return listOfNotNull(initialMessage)
    }

    private suspend fun handleResourceMessage(
        chat: ChatDto,
        lastMessage: MessageDto,
        conversation: List<ChatMessage>,
        previousChats: List<ChatDto>,
        regenerateMessage: MessageDto?,
        resources: List<ResourceDto>
    ): List<MessageDto> {
        Napier.v("Handling resource message")
        val lastMessageResources = lastMessage.resourceIds.mapNotNull { id -> resources.find { it.id == id } }
        val messages = mutableListOf<MessageDto>()

        lastMessageResources.groupBy { it.type.substringBefore('/') }.forEach { (type, res) ->
            when (type) {
                CONTENT_TYPE_IMAGE -> {
                    Napier.v("Resource is an image, answering with text")
                    val answer = answerChatWithTextAndContext(chat, conversation, previousChats)
                    val newMessage = upsertMessage(toNewMessage(regenerateMessage, chat, answer))
                    messages.add(newMessage)
                }
                CONTENT_TYPE_AUDIO -> {
                    Napier.v("Resource is audio, transcribing and answering with text and audio")
                    val transcriptionResult = handleAudioResource(chat, res, conversation, previousChats, regenerateMessage, lastMessage)
                    messages.addAll(transcriptionResult)
                }
                else -> Napier.w("Unsupported resource type: $type")
            }
        }
        return messages
    }

    private suspend fun handleAudioResource(
        chat: ChatDto,
        resources: List<ResourceDto>,
        conversation: List<ChatMessage>,
        previousChats: List<ChatDto>,
        regenerateMessage: MessageDto?,
        originalMessage: MessageDto
    ): List<MessageDto> {
        val speechToTextContents = resources.mapNotNull { resource ->
            transcribeAudio(chat, resource)
        }

        if (speechToTextContents.size != resources.size) {
            Napier.w("Failed to transcribe all audio messages")
        }

        val lastMessageInConversation = conversation.last()
        val newContent = ((lastMessageInConversation.content?.let { "$it\n\n" } ?: "") + speechToTextContents.joinToString("\n\n")).trim()

        val updatedOriginalMessage = upsertMessage(originalMessage.copy(content = newContent))

        val modifiedConversation = conversation.dropLast(1) + ChatMessage(
            role = lastMessageInConversation.role,
            content = newContent
        )

        val answer = answerChatWithTextAndContext(chat, modifiedConversation, previousChats)
        val audioResource = generateSpeechFromText(chat, answer)
        val chatCompletionModelConfig = chat.models[Capability.CHAT_COMPLETION]!!
        val newResourceId = audioResource?.let { createResource(chat.id, SenderType.Ai(chatCompletionModelConfig), it).id }
        val newMessage = upsertMessage(toNewMessage(regenerateMessage, chat, answer, listOfNotNull(newResourceId)))

        return listOf(updatedOriginalMessage, newMessage)
    }

    private fun updateChatNameAndSummary(
        chat: ChatDto,
        messages: List<MessageDto>,
        resources: List<ResourceDto>
    ) {
        val conversation = messages.toConversation(chat.ownerId.toString(), resources)
        var currentChat = chat
        coroutineScopeIO.launch {
            combine(
                generateNewChatNameIfNeededStream(currentChat, conversation),
                generateSummaryForChatIfNeededStream(currentChat, conversation)
            ) { name, summary ->
                val newName = name ?: currentChat.name
                val newSummary = summary ?: currentChat.summary
                if (newName != currentChat.name || newSummary != currentChat.summary) {
                    currentChat = upsertChat(currentChat.copy(name = newName, summary = newSummary))
                }
            }.catch {
                Napier.e("Error while generating name and summary", it)
            }.launchIn(this)
        }
    }

    private fun toNewMessage(
        regeneratedMessage: MessageDto?,
        chat: ChatDto,
        answer: String,
        resourceIds: List<Uuid> = emptyList()
    ) = regeneratedMessage?.copy(
        content = answer,
        resourceIds = resourceIds.ifEmpty { regeneratedMessage.resourceIds }
    ) ?: MessageDto(
        id = Uuid.random(),
        sender = SenderType.Ai(chat.models[Capability.CHAT_COMPLETION]!!),
        chatId = chat.id,
        content = answer,
        time = Clock.System.now().toEpochMilliseconds(),
        resourceIds = resourceIds
    )

    private fun generateAttributeStream(
        chatId: Uuid,
        currentValue: String?,
        attributeName: String,
        prompt: String,
        modelConfig: AiSource,
        conversation: List<ChatMessage>
    ): Flow<String?> {
        if (!currentValue.isNullOrBlank()) {
            Napier.v { "Not generating new chat $attributeName, already exists: \"$currentValue\"" }
            return flowOf(null)
        }
        Napier.v { "Generating new chat $attributeName for chat $chatId" }
        val newConversation = conversation + ChatMessage(role = ChatRole.System, content = prompt)
        return answerChatWithTextStream(modelConfig, newConversation)
    }

    private fun generateSummaryForChatIfNeededStream(
        chat: ChatDto,
        conversation: List<ChatMessage>,
    ): Flow<String?> {
        val prompt = "\n\nPlease provide a summary of this conversation. No more than 200 characters. ANSWER WITH ONLY THE SUMMARY, NOTHING ELSE."
        return generateAttributeStream(chat.id, chat.summary, "summary", prompt, chat.models[Capability.CHAT_COMPLETION]!!, conversation)
    }

    private fun generateNewChatNameIfNeededStream(
        chat: ChatDto,
        conversation: List<ChatMessage>,
    ): Flow<String?> {
        val prompt = "\n\nPlease provide the name for this conversation about a few words. No more than 40 characters. ANSWER WITH ONLY THE NAME, NOTHING ELSE."
        return generateAttributeStream(chat.id, chat.name, "name", prompt, chat.models[Capability.CHAT_COMPLETION]!!, conversation)
    }

    private suspend fun answerChatWithTextAndContext(
        chat: ChatDto,
        conversation: List<ChatMessage>,
        previousChats: List<ChatDto>,
    ): String {
        val chatSummaries = previousChats.mapNotNull { it.summary }
        val aiSource = chat.models[Capability.CHAT_COMPLETION]!!
        val openAI = getOpenAIClient(aiSource.endpoint)
        return openAI.chatCompletion(
            request = ChatCompletionRequest(
                model = ModelId(aiSource.modelId),
                messages = if (chatSummaries.isEmpty()) conversation else conversation + ChatMessage(
                    role = ChatRole.System,
                    content = "Previous chat summaries:\n${chatSummaries.joinToString("\n")}".also {
                        Napier.v { "Context for answer:\n$it" }
                    }
                )
            )
        ).choices.first().message.content!!
    }

    private fun answerChatWithTextAndContextStream(
        chat: ChatDto,
        conversation: List<ChatMessage>,
        previousChats: List<ChatDto>,
    ) = flow {
        val chatSummaries = previousChats.mapNotNull { it.summary }
        val aiSource = chat.models[Capability.CHAT_COMPLETION]!!
        val openAI = getOpenAIClient(aiSource.endpoint)
        var previousCompletions: String? = ""
        emitAll(openAI.chatCompletions(
            request = ChatCompletionRequest(
                model = ModelId(aiSource.modelId),
                messages = conversation,
                // FIXME: providing memory should be optional if AI requests it.
                //  Try to provide a function which returns the most recent chat summaries.
                //  Local function call is not yet supported? Disabling context for now.
//                messages = if (chatSummaries.isEmpty()) conversation else conversation + ChatMessage(
//                    role = ChatRole.System,
//                    content = "Previous chat summaries:\n${chatSummaries.joinToString("\n")}".also {
//                        Napier.v { "Context for answer:\n$it" }
//                    }
//                )
            ),
        ).map { completionChunk ->
            previousCompletions = try {
                val choice = completionChunk.choices.first()
                if (choice.finishReason != null) {
                    Napier.v("Chat completion finished with reason: ${choice.finishReason}")
                    return@map null
                }
                previousCompletions + (choice.delta?.content ?: "")
            } catch (e: Exception) {
                Napier.e("Error while streaming chat completion", e); null
            }
            previousCompletions
        })
    }

    private fun answerChatWithTextStream(
        aiSource: AiSource,
        conversation: List<ChatMessage>,
    ) = flow {
        var previousCompletions: String? = ""
        emitAll(getOpenAIClient(aiSource.endpoint).chatCompletions(
            request = ChatCompletionRequest(
                model = ModelId(aiSource.modelId),
                messages = conversation.also { conversation ->
                    Napier.v { "Context for answer: ${conversation.joinToString("\n") { chatMessage ->
                        "Role: ${chatMessage.role.role} \tMessage: ${
                        when (val content = chatMessage.messageContent) {
                            is TextContent -> content.content
                            is ListContent -> content.content.joinToString("\n") { 
                                when (it) {
                                    is TextPart -> it.text
                                    is ImagePart -> "Image url: ${it.imageUrl.url}"
                                }
                            }
                            null -> "Unknown content type"
                        }
                    }" }}" }
                }
            )
        ).map { completionChunk ->
            previousCompletions = try {
                val choice = completionChunk.choices.first()
                if (choice.finishReason != null) {
                    Napier.v("Chat completion finished with reason: ${choice.finishReason}")
                    return@map null
                }
                previousCompletions + (choice.delta?.content ?: "")
            } catch (e: Exception) {
                Napier.e("Error while streaming chat completion", e)
                null
            }
            previousCompletions
        })
    }

    private suspend fun transcribeAudio(
        chat: ChatDto,
        resource: ResourceDto,
    ): String? {
        // get chat history with chatId
        // transcribe audio with whisper or other speech to text model
        // answer the transcribed text
        if (!chat.models.contains(Capability.AUDIO_TRANSCRIPTION)) {
            Napier.v { "No audio transcription model for chat ${chat.id}" }
            return null
        }
        val aiSource = chat.models[Capability.AUDIO_TRANSCRIPTION]!!
        val openAI = getOpenAIClient(aiSource.endpoint)
        val type = resource.type.split("/").last()
        // Create tmp file for audio
        val audioFilePath = kotlin.io.path.createTempFile("${resource.id}_audio_file", ".$type")
        audioFilePath.toFile().writeBytes(resource.data)
        return openAI.transcription(
            request = TranscriptionRequest(
                audio = FileSource(name = audioFilePath.toFile().name, source = audioFilePath.toFile().inputStream().asSource()),
                model = ModelId(aiSource.modelId),
                responseFormat = AudioResponseFormat.Text,
            )
        ).text.also { audioFilePath.toFile().delete() }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun generateSpeechFromText(
        chat: ChatDto,
        text: String,
    ): ResourceDto? {
        // generate audio from text
        // return audio resource
        if (!chat.models.contains(Capability.SPEECH_SYNTHESIS)) {
            Napier.v { "No speech synthesis model for chat ${chat.id}" }
            return null
        }
        val aiSource = chat.models[Capability.SPEECH_SYNTHESIS]!!
        val openAI = getOpenAIClient(aiSource.endpoint)
        return ResourceDto(
            type = "audio/mp3",
            data = openAI.speech(
                request = SpeechRequest(
                    model = ModelId(aiSource.modelId),
                    input = text,
                    responseFormat = SpeechResponseFormat.Mp3,
                    voice = Voice.Alloy
                )
            )
        )
    }
}

@OptIn(ExperimentalUuidApi::class)
fun List<MessageDto>.toConversation(userId: String, resources: List<ResourceDto> = emptyList()): List<ChatMessage> {
    return fold<MessageDto, MutableList<ChatMessage>>(mutableListOf()) { acc, message ->
        val previousAndCurrentMessageFromAssistant = acc.lastOrNull()?.role == ChatRole.Assistant && message.senderId != userId
        val previousAndCurrentMessageFromUser = acc.lastOrNull()?.role == ChatRole.User && message.senderId == userId
        if (previousAndCurrentMessageFromAssistant || previousAndCurrentMessageFromUser) {
            val previousMessageContent = (acc.last().messageContent.takeIf { it is TextContent } as? TextContent)?.content
            acc.removeAt(acc.lastIndex)
            acc.add(message.toChatMessage(userId, previousMessageContent, resources))
            return@fold acc
        }
        acc.add(message.toChatMessage(userId, resources = resources))
        acc
    }.also {
        Napier.v { "Converted $size messages to conversation" }
    }
}

@OptIn(ExperimentalEncodingApi::class, ExperimentalUuidApi::class)
fun MessageDto.toChatMessage(
    userId: String,
    previousMessageContent: String? = null,
    resources: List<ResourceDto> = emptyList(),
): ChatMessage {
    val textPart = if (previousMessageContent?.trim('\n').isNullOrBlank()) content ?: "" else if (content == null) previousMessageContent else "$previousMessageContent\n$content"
    val content = mutableListOf<ContentPart>()
    resources.filter { it.type.startsWith("image") && resourceIds.contains(it.id) }.forEach { imageResource ->
        val imageData = "data:${imageResource.type};base64,${Base64.encode(imageResource.data)}"
        content += ImagePart(imageData)
    }
    if (textPart.isNotBlank()) content += TextPart(textPart)
    return if (content.size == 1 && content[0] is TextPart) {
        // Only text
        ChatMessage(
            role = if (userId == senderId) ChatRole.User else ChatRole.Assistant,
            content = textPart
        )
    } else {
        // Image with text
        ChatMessage(
            role = if (userId == senderId) ChatRole.User else ChatRole.Assistant,
            content = content
        )
    }
}

