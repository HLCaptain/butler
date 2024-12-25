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
import com.aallam.openai.api.chat.TextContent
import com.aallam.openai.api.chat.TextPart
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.MessageDto
import illyan.butler.shared.model.chat.ResourceDto
import io.github.aakira.napier.Napier
import kotlinx.datetime.Clock
import kotlinx.io.asSource
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class LlmService(
    private val getResource: suspend (resourceId: String) -> ResourceDto,
    private val createResource: suspend (chatId: String, modelId: String, resource: ResourceDto) -> ResourceDto,
    private val upsertMessage: suspend (message: MessageDto) -> MessageDto,
    private val getOpenAIClient: suspend (endpoint: String) -> OpenAI
) {
    suspend fun answerChat(
        chat: ChatDto,
        previousChats: List<ChatDto> = emptyList(),
        regenerateMessage: MessageDto? = null
    ) {
        val chatModelId = chat.chatCompletionModel!!.second
        val messages = chat.lastFewMessages.sortedBy { it.time }
        if (messages.isEmpty()) return
        // Then last message is by a user
        val lastMessage = if (regenerateMessage != null) {
            messages.first { it.id == regenerateMessage.id }
        } else {
            messages.last()
        }
        val resources = messages.map { message -> message.resourceIds.map { getResource(it) } }.flatten()
        val conversation = messages.takeWhile { it.time!! <= lastMessage.time!! }.toConversation(chat.ownerId, resources)
        if (lastMessage.senderId != chat.ownerId) {
            generateSummaryForChatIfNeeded(chat, conversation)
            generateNewChatNameIfNeeded(chat, conversation)
            return
        }

        if (lastMessage.resourceIds.isEmpty()) {
            // Last message is simple text
            // Answer with simple text
            Napier.v("Last message is simple text, answering with text")
            val answer = answerChatWithTextAndContext(chat, conversation, previousChats)
            upsertMessage(generateNewMessage(regenerateMessage, chat, answer))
            val newConversation = conversation + listOf(MessageDto(senderId = chatModelId, message = answer, chatId = chat.id!!)).toConversation(chat.ownerId)
            generateNewChatNameIfNeeded(
                chat,
                newConversation,
            )
            generateSummaryForChatIfNeeded(
                chat,
                newConversation,
            )
        } else {
            // Describe image
            // For audio, reply with text and audio
            val lastMessageResources = lastMessage.resourceIds.mapNotNull { id -> resources.find { it.id == id } }
            lastMessageResources.forEach { resource ->
                when (resource.type.split("/").first()) {
                    "image" -> {
                        Napier.v("Resource is an image, answering with text")
                        val answer = answerChatWithTextAndContext(chat, conversation, previousChats)
                        upsertMessage(generateNewMessage(regenerateMessage, chat, answer))
                        val newConversation = conversation + listOf(MessageDto(senderId = chatModelId, message = answer, chatId = chat.id!!)).toConversation(chat.ownerId)
                        generateNewChatNameIfNeeded(
                            chat,
                            newConversation,
                        )
                        generateSummaryForChatIfNeeded(
                            chat,
                            newConversation,
                        )
                    }
                    "audio" -> {
                        Napier.v("Resource is audio, transcribing and answering with text and audio")
                        val speechToTextContent = transcribeAudio(chat, resource)
                        val modifiedConversation = conversation.toMutableList()
                        val lastMessageContent = ((modifiedConversation.lastOrNull()?.content?.let { "$it\n\n" } ?: "") + speechToTextContent).trim('\n', ' ')
                        modifiedConversation.removeLast()
                        modifiedConversation.add(
                            lastMessage.toChatMessage(
                                userId = chat.ownerId,
                                previousMessageContent = lastMessageContent,
                                resources = listOf(resource)
                            )
                        )
                        // Append transcription to last message
                        upsertMessage(lastMessage.copy(message = lastMessageContent))
                        val answer = answerChatWithTextAndContext(chat, modifiedConversation, previousChats)
                        val audioResource = generateSpeechFromText(chat, answer)
                        // Upload resource
                        // Send message with resourceId
                        val newResourceId = createResource(chat.id!!, chatModelId, audioResource).id!!
                        upsertMessage(generateNewMessage(regenerateMessage, chat, answer, listOf(newResourceId)))
                        val additionalConversation = listOf(
                            MessageDto(
                                senderId = chatModelId,
                                message = answer,
                                resourceIds = listOf(newResourceId),
                                chatId = chat.id!!
                            )
                        ).toConversation(chat.ownerId, listOf(resource))
                        generateNewChatNameIfNeeded(
                            chat,
                            modifiedConversation + additionalConversation,
                        )
                        generateSummaryForChatIfNeeded(
                            chat,
                            modifiedConversation + additionalConversation,
                        )
                    }
                    else -> Napier.v { "Resource type ${resource.type} not supported" }
                }
            }
        }
    }

    private fun generateNewMessage(
        regeneratedMessage: MessageDto?,
        chat: ChatDto,
        answer: String,
        resourceIds: List<String> = regeneratedMessage?.resourceIds ?: emptyList()
    ) = MessageDto(
        id = regeneratedMessage?.id,
        senderId = chat.chatCompletionModel!!.second,
        chatId = chat.id!!,
        message = answer,
        time = regeneratedMessage?.time ?: Clock.System.now().toEpochMilliseconds(),
        resourceIds = resourceIds
    )

    private suspend fun generateSummaryForChatIfNeeded(
        chat: ChatDto,
        conversation: List<ChatMessage>,
    ): String {
        if (!chat.summary.isNullOrBlank()) return chat.summary!!
        val newChatSummaryPrompt = "\n\nPlease provide a summary of this conversation. No more than 200 characters. ANSWER WITH ONLY THE SUMMARY, NOTHING ELSE."
        val newConversation = conversation + ChatMessage(role = ChatRole.System, messageContent = TextContent(newChatSummaryPrompt))
        return answerChatWithText(chat.chatCompletionModel!!.second, chat.chatCompletionModel!!.first, newConversation)
    }

    /**
     * Last message should be from AI.
     */
    private suspend fun generateNewChatNameIfNeeded(
        chat: ChatDto,
        conversation: List<ChatMessage>,
    ): String {
        if (!chat.name.isNullOrBlank()) return chat.name!!
        val newChatNamePrompt = "\n\nPlease provide the name for this conversation about a few words. No more than 60 characters. ANSWER WITH ONLY THE NAME, NOTHING ELSE."
        val newConversation = conversation + ChatMessage(role = ChatRole.System, messageContent = TextContent(newChatNamePrompt))
        return answerChatWithText(chat.chatCompletionModel!!.second, chat.chatCompletionModel!!.first, newConversation)
    }

    private suspend fun answerChatWithTextAndContext(
        chat: ChatDto,
        conversation: List<ChatMessage>,
        previousChats: List<ChatDto>,
    ): String {
        val chatSummaries = previousChats.mapNotNull { it.summary }
        val openAI = getOpenAIClient(chat.chatCompletionModel!!.first)
        return openAI.chatCompletion(
            request = ChatCompletionRequest(
                model = ModelId(chat.chatCompletionModel!!.second),
                messages = if (chatSummaries.isEmpty()) conversation else conversation + ChatMessage(
                    role = ChatRole.System,
                    content = "Previous chat summaries: ${chatSummaries.joinToString("\n")}"
                )
            )
        ).choices.first().message.content!!
    }

    private suspend fun answerChatWithText(
        modelId: String,
        endpoint: String,
        conversation: List<ChatMessage>,
    ): String {
        return getOpenAIClient(endpoint).chatCompletion(
            request = ChatCompletionRequest(
                model = ModelId(modelId),
                messages = conversation
            )
        ).choices.first().message.content!!
    }

    private suspend fun transcribeAudio(
        chat: ChatDto,
        resource: ResourceDto,
    ): String {
        // get chat history with chatId
        // transcribe audio with whisper or other speech to text model
        // answer the transcribed text
        val openAI = getOpenAIClient(chat.audioTranscriptionModel!!.first)
        val type = resource.type.split("/").last()
        // Create tmp file for audio
        val audioFilePath = kotlin.io.path.createTempFile("${resource.id}_audio_file", ".$type")
        audioFilePath.toFile().writeBytes(resource.data)
        return openAI.transcription(
            request = TranscriptionRequest(
                audio = FileSource(name = audioFilePath.toFile().name, source = audioFilePath.toFile().inputStream().asSource()),
                model = ModelId(chat.audioTranscriptionModel!!.second),
                responseFormat = AudioResponseFormat.Text,
            )
        ).text.also { audioFilePath.toFile().delete() }
    }

    private suspend fun generateSpeechFromText(
        chat: ChatDto,
        text: String,
    ): ResourceDto {
        // generate audio from text
        // return audio resource
        val openAI = getOpenAIClient(chat.audioSpeechModel!!.first)
        return ResourceDto(
            type = "audio/mp3",
            data = openAI.speech(
                request = SpeechRequest(
                    model = ModelId(chat.audioSpeechModel!!.second),
                    input = text,
                    responseFormat = SpeechResponseFormat.Mp3,
                    voice = Voice.Alloy
                )
            )
        )
    }
}

fun List<MessageDto>.toConversation(userId: String, resources: List<ResourceDto> = emptyList()): List<ChatMessage> {
    val currentMessages = this
    return fold<MessageDto, MutableList<ChatMessage>>(mutableListOf()) { acc, message ->
        val previousAndCurrentMessageFromAssistant = acc.lastOrNull()?.role == ChatRole.Assistant && message.senderId != userId
        val previousAndCurrentMessageFromUser = acc.lastOrNull()?.role == ChatRole.User && message.senderId == userId
        if (previousAndCurrentMessageFromAssistant || previousAndCurrentMessageFromUser) {
            val previousMessageContent = acc.last().content
            acc.removeLast()
            acc.add(message.toChatMessage(userId, previousMessageContent, resources))
            return@fold acc
        }
        acc.add(message.toChatMessage(userId, resources = resources))
        acc
    }.also {
        Napier.v { "Converted messages $currentMessages to conversation: $it" }
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun MessageDto.toChatMessage(
    userId: String,
    previousMessageContent: String? = null,
    resources: List<ResourceDto> = emptyList(),
): ChatMessage {
    val textPart = if (previousMessageContent?.trim('\n').isNullOrBlank()) message ?: "" else if (message == null) previousMessageContent ?: "" else "$previousMessageContent\n$message"
    val content = mutableListOf<ContentPart>()
    resources.filter { it.type.startsWith("image") && resourceIds.contains(it.id) }.forEach { imageResource ->
        val imageData = "data:${imageResource.type};base64,${Base64.encode(imageResource.data)}"
        content += ImagePart(imageData)
    }
    if (textPart.isNotBlank()) content += TextPart(textPart)
    return if (content.size <= 1 && content[0] is TextPart) {
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