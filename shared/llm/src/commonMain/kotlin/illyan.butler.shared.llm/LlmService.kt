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

@OptIn(ExperimentalUuidApi::class)
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
    private val lastFewMessages = mutableMapOf<Uuid, List<MessageDto>>()
    @OptIn(ExperimentalTime::class)
    suspend fun answerChat(
        chat: ChatDto,
        chatMessages: List<MessageDto>,
        previousChats: List<ChatDto> = emptyList(),
        regenerateMessage: MessageDto? = null
    ) {
        var chat = chat
        Napier.v("Answering chat ${chat.id}")
        val chatCompletionModelConfig = chat.models[Capability.CHAT_COMPLETION]!!
        var messages = chatMessages.sortedBy { it.time }
        if (messages.isEmpty()) {
            Napier.v("No messages in chat, skipping")
            return
        }
        // Then last message is by a user
        var lastMessage = if (regenerateMessage != null) {
            messages.first { it.id == regenerateMessage.id }
        } else {
            messages.last()
        }
        val resources = messages.map { message -> message.resourceIds.map { getResource(it, chat.ownerId) } }.flatten()
        var conversation = messages.takeWhile { it.time!! <= lastMessage.time!! }.toConversation(chat.ownerId.toString(), resources)
        if (lastMessage.senderId != chat.ownerId.toString() && lastMessage.content.isNullOrBlank()) {
            Napier.v("Last message from AI is blank, regenerating new response.")
            removeMessage(lastMessage)
            // Removing last message from conversation
            messages = messages.filter { lastMessage.id != it.id }
            if (messages.isEmpty()) {
                Napier.v("No messages in chat, skipping")
                return
            }
            lastFewMessages[chat.id] = messages
            conversation = messages.takeWhile { it.time!! <= lastMessage.time!! }.toConversation(chat.ownerId.toString(), resources)
            lastMessage = if (regenerateMessage != null) {
                messages.first { it.id == regenerateMessage.id }
            } else {
                messages.last()
            }
        }
        if (lastMessage.senderId != chat.ownerId.toString() && regenerateMessage == null && !lastMessage.content.isNullOrBlank()) {
            var updatedChat = chat
            coroutineScopeIO.launch {
                combine(
                    generateNewChatNameIfNeededStream(chat, conversation),
                    generateSummaryForChatIfNeededStream(chat, conversation)
                ) { name, summary ->
                    updatedChat = when {
                        name != null && summary != null -> upsertChat(updatedChat.copy(name = name, summary = summary))
                        name != null -> upsertChat(updatedChat.copy(name = name))
                        summary != null -> upsertChat(updatedChat.copy(summary = summary))
                        else -> {
                            Napier.v { "Name and summary generation ended. Chat name: ${updatedChat.name}, summary: ${updatedChat.summary}" }
                            cancel()
                            return@combine
                        }
                    }
                }.catch {
                    Napier.e("Error while generating name and summary", it)
                    cancel()
                }.launchIn(this)
            }
            Napier.v("Last message is not by user, skipping")
            return
        }
        val updatedChat = if (lastMessage.resourceIds.isEmpty()) {
            // Last message is simple text
            // Answer with simple text
            Napier.v("Last message is simple text, answering with text")
            val answerFlow = answerChatWithTextAndContextStream(chat, conversation, previousChats)
            var updatedAnswer: MessageDto? = null
            try {
                // Getting the first response
                updatedAnswer = answerFlow.firstOrNull { !it.isNullOrBlank() }?.let { toNewMessage(regenerateMessage, chat, it) }
            } catch (e: Exception) {
                Napier.e("Error while getting answer", e)
                errorInMessageResponse(null)
                return
            }
            // Inserting the response, getting new local UUID
            updatedAnswer = updatedAnswer?.let { upsertMessage(it) }
            // Listening for further responses and updating the message
            coroutineScopeIO.launch {
                answerFlow.catch {
                    Napier.e("Error accepting chat completion stream", it)
                    updatedAnswer?.let { errorInMessageResponse(it) }
                    cancel()
                }.collect { answer ->
                    // Stopping on null: answering ended
                    if (answer == null) {
                        Napier.v("Answer is null, completion ended, final answer: $updatedAnswer")
                        cancel()
                        return@collect
                    }
                    // Getting updated message instance and updating it
                    updatedAnswer = toNewMessage(updatedAnswer ?: regenerateMessage, chat, answer)
                    updatedAnswer = updatedAnswer?.let { upsertMessage(it) }
                }
            }
            // Updating the chat with new message instance (message will get updated using listener above)
            updatedAnswer?.let { answer ->
                lastFewMessages[chat.id!!] = lastFewMessages[chat.id].orEmpty().filter { it.id != answer.id } + answer
            }
            chat
        } else {
            // Describe image
            // For audio, reply with text and audio
            val lastMessageResources = lastMessage.resourceIds.mapNotNull { id -> resources.find { it.id == id } }
            lastMessageResources.groupBy { it.type }.map { (type, resources) ->
                when (type.split("/").first()) {
                    "image" -> {
                        Napier.v("Resource is an image, answering with text")
                        val answer = answerChatWithTextAndContext(chat, conversation, previousChats)
                        val newMessage = upsertMessage(toNewMessage(regenerateMessage, chat, answer))
                        lastFewMessages[chat.id] = (lastFewMessages[chat.id].orEmpty().filter { it.id != newMessage.id } + newMessage)
                    }
                    "audio" -> {
                        Napier.v("Resource is audio, transcribing and answering with text and audio")
                        val speechToTextContents = resources.map { resource ->
                            if (resource.type.startsWith("audio")) {
                                transcribeAudio(chat, resource)
                            } else {
                                null
                            }
                        }
                        if (speechToTextContents.any { it == null }) {
                            Napier.v("Failed to transcribe ${speechToTextContents.filter { it == null }.size} audio messages, skipping")
                            return
                        }
                        val modifiedConversation = conversation.toMutableList()
                        val lastMessageContent = ((modifiedConversation.lastOrNull()?.content?.let { "$it\n\n" } ?: "") + speechToTextContents.joinToString("\n\n")).trim('\n', ' ')
                        modifiedConversation.removeAt(modifiedConversation.lastIndex)
                        modifiedConversation.add(
                            lastMessage.toChatMessage(
                                userId = chat.ownerId.toString(),
                                previousMessageContent = lastMessageContent,
                                resources = resources
                            )
                        )
                        // Append transcription to last message
                        val transcriptions = upsertMessage(lastMessage.copy(content = lastMessageContent))
                        val answer = answerChatWithTextAndContext(chat, modifiedConversation, previousChats)
                        val audioResource = generateSpeechFromText(chat, answer)
                        // Upload resource
                        // Send message with resourceId
                        val newResourceId = audioResource?.let { createResource(chat.id, SenderType.Ai(chatCompletionModelConfig), it).id }
                        val newMessage = upsertMessage(toNewMessage(regenerateMessage, chat, answer, listOfNotNull(newResourceId)))
                        val newMessages = listOf(transcriptions, newMessage).map { it.id }
                        lastFewMessages[chat.id] = (lastFewMessages[chat.id].orEmpty().filter { it.id !in newMessages } + transcriptions + newMessage)
                    }
                    else -> {
                        Napier.v { "Resource type $type not supported" }
                    }
                }
            }
            chat
        }
        val updatedConversation = lastFewMessages[chat.id]!!.sortedBy { it.time }.toConversation(chat.ownerId.toString(), resources)
        coroutineScopeIO.launch {
            var freshChat = updatedChat
            combine(
                generateNewChatNameIfNeededStream(updatedChat, updatedConversation),
                generateSummaryForChatIfNeededStream(updatedChat, updatedConversation)
            ) { chatName, chatSummary ->
                freshChat = when {
                    chatName != null && chatSummary != null -> upsertChat(freshChat.copy(name = chatName, summary = chatSummary))
                    chatName != null -> upsertChat(freshChat.copy(name = chatName))
                    chatSummary != null -> upsertChat(freshChat.copy(summary = chatSummary))
                    else -> {
                        Napier.v { "Name and summary generation ended. Chat name: ${freshChat.name}, summary: ${freshChat.summary}" }
                        cancel()
                        freshChat
                    }
                }
            }.launchIn(this)
        }
    }

    private fun toNewMessage(
        regeneratedMessage: MessageDto?,
        chat: ChatDto,
        answer: String,
        resourceIds: List<Uuid> = regeneratedMessage?.resourceIds ?: emptyList()
    ) = MessageDto(
        id = regeneratedMessage?.id ?: Uuid.random(),
        sender = SenderType.Ai(chat.models[Capability.CHAT_COMPLETION]!!),
        chatId = chat.id,
        content = answer,
        time = regeneratedMessage?.time ?: Clock.System.now().toEpochMilliseconds(),
        resourceIds = resourceIds
    )

    private fun generateSummaryForChatIfNeededStream(
        chat: ChatDto,
        conversation: List<ChatMessage>,
    ): Flow<String?> {
        if (!chat.summary.isNullOrBlank()) {
            Napier.v { "Not generating new chat summary, already exists: \"${chat.summary}\"" }
            return flowOf(null)
        }
        Napier.v { "Generating new chat summary for chat ${chat.id}" }
        val newChatSummaryPrompt = "\n\nPlease provide a summary of this conversation. No more than 200 characters. ANSWER WITH ONLY THE SUMMARY, NOTHING ELSE."
        val newConversation = conversation + ChatMessage(role = ChatRole.System, messageContent = TextContent(newChatSummaryPrompt))
        val modelConfig = chat.models[Capability.CHAT_COMPLETION]!!
        return answerChatWithTextStream(modelConfig, newConversation)
    }

    private fun generateNewChatNameIfNeededStream(
        chat: ChatDto,
        conversation: List<ChatMessage>,
    ): Flow<String?> {
        if (!chat.name.isNullOrBlank()) {
            Napier.v { "Not generating new chat name, already exists: \"${chat.name}\"" }
            return flowOf(null)
        }
        Napier.v { "Generating new chat name for chat ${chat.id}" }
        val newChatNamePrompt = "\n\nPlease provide the name for this conversation about a few words. No more than 40 characters. ANSWER WITH ONLY THE NAME, NOTHING ELSE."
        val newConversation = conversation + ChatMessage(role = ChatRole.System, messageContent = TextContent(newChatNamePrompt))
        return answerChatWithTextStream(chat.models[Capability.CHAT_COMPLETION]!!, newConversation)
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
