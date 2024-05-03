package illyan.butler.services.ai.data.service

import com.aallam.openai.api.audio.SpeechRequest
import com.aallam.openai.api.audio.SpeechResponseFormat
import com.aallam.openai.api.audio.TranscriptionRequest
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
import illyan.butler.services.ai.AppConfig
import illyan.butler.services.ai.data.model.ai.ModelDto
import illyan.butler.services.ai.data.model.chat.ChatDto
import illyan.butler.services.ai.data.model.chat.MessageDto
import illyan.butler.services.ai.data.model.chat.ResourceDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import okio.source
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Single
class LlmService(
    private val modelHealthService: ModelHealthService,
    private val chatService: ChatService,
    @Named("OpenAIClients") private val openAIClients: Map<String, OpenAI>,
    private val coroutineScope: CoroutineScope
) {
    // All messages per model
    // Messages can be grouped and sorted by chatId
    private val chatsByModels = hashMapOf<String, List<ChatDto>>()
    private val awaitingChatReplies = hashMapOf<String, List<String>>()
    private var modelsAndProviderIds = mapOf<ModelDto, List<String>>()
    private val resources: MutableMap<String, ResourceDto> = mutableMapOf()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun loadModels() {
        coroutineScope.launch {
            modelHealthService.modelsAndProviders.flatMapLatest { modelsAndProviders ->
                modelsAndProviderIds = modelsAndProviders
                modelsAndProviders.map { (model, _) ->
                    flow {
                        Napier.v("Processing model with id: ${model.id}")
                        if (!chatsByModels.containsKey(model.id)) {
                            Napier.v("Model with id: $model not found in chatsByModels. Fetching chats.")
                            while (true) {
                                val chats = try {
                                    chatService.getChats(model.id)
                                } catch (e: Exception) {
                                    Napier.e("Error fetching chats for model with id: ${model.id}", e)
                                    emptyList()
                                }
                                chatsByModels[model.id] = chats
                                chats.forEach { chat ->
                                    chat.lastFewMessages.forEach { message ->
                                        message.resourceIds.forEach { resourceId ->
                                            if (!resources.containsKey(resourceId)) {
                                                resources[resourceId] = chatService.getResource(model.id, resourceId)
                                            }
                                        }
                                    }
                                }
                                Napier.v("Fetched ${chats.size} chats for model with id: ${model.id}")
                                emit(chatsByModels[model.id]!!.mapNotNull { it.id }.toSet())
                                delay(60000L)
                            }
                        }
                    }
                }.merge().flowOn(Dispatchers.IO)
            }.collect { chatIds -> updateChatsIfNeeded(chatIds) }
        }
    }

    private suspend fun updateChatsIfNeeded(chatIds: Set<String>) = chatIds.forEach { sendMessageIfNeeded(it) }

    // TODO: mark chat with service instance id that it generates messages currently, so that other instances don't generate messages for the same chat.
    //  Should modify chat service to do so.
    //  Don't generate messages for marked chats.
    //  If a chat is marked, the client could indicate that the messages are being generated.
    private suspend fun sendMessageIfNeeded(chatId: String) {
        answerChat(chatId)
    }

    /**
     * Regenerate a message for a chat.
     * @param chatId The chat ID to regenerate a message for.
     * @param messageId The message ID to regenerate. If null, a new message will be generated.
     *
     * Updated or inserts new message for chatId.
     */
    suspend fun regenerateMessage(chatId: String, messageId: String?) {
        // TODO: Generate new message for chatId
        if (messageId != null) {
            // Edit message with messageId
            val modelId = chatsByModels.keys.find { modelId -> chatsByModels[modelId]?.any { it.id == chatId } == true }
            val chat = chatsByModels[modelId]?.firstOrNull { it.id == chatId }
            val message = chat?.lastFewMessages?.firstOrNull { it.id == messageId } ?: return
            answerChat(chatId, message)
        } else {
            // Add new message
            answerChat(chatId)
        }
    }

    suspend fun answerChat(chatId: String, regenerateMessage: MessageDto? = null) {
        val modelId = chatsByModels.keys.find { modelId -> chatsByModels[modelId]?.any { it.id == chatId } == true }
        val chat = chatsByModels[modelId]?.firstOrNull { it.id == chatId }
        val messages = chat?.lastFewMessages?.sortedBy { it.time } ?: return
        if (messages.isEmpty() || messages.last().senderId == modelId) return
        // Then last message is by a user
        val lastMessage = if (regenerateMessage != null) {
            messages.first { it.id == regenerateMessage.id }
        } else {
            messages.last()
        }
        val resources = lastMessage.resourceIds.map { resources.getOrPut(it) { chatService.getResource(modelId!!, it) } }
        val conversation = messages.takeWhile { it.time!! <= lastMessage.time!! }.toConversation(listOf(modelId!!), resources)
        if (awaitingChatReplies[modelId]?.contains(chatId) != true) {
            awaitingChatReplies[modelId] = ((awaitingChatReplies[modelId] ?: emptyList()) + chatId).distinct()
            val modelEndpoint = chat.aiEndpoints[modelId] ?: AppConfig.Api.LOCAL_AI_OPEN_AI_API_URL // Use local AI (self-hosting) as default
            if (lastMessage.resourceIds.isEmpty()) {
                // Last message is simple text
                // Answer with simple text
                Napier.v("Last message is simple text, answering with text")
                val answer = answerChatWithText(modelId, modelEndpoint, conversation)
                upsertNewMessage(regenerateMessage, modelId, chatId, answer)
            } else {
                // Describe image
                // For audio, reply with text and audio
                resources.forEach { resource ->
                    when (resource.type.split("/").first()) {
                        "image" -> {
                            Napier.v("Resource is an image, answering with text")
                            val answer = answerChatWithText("gpt-4-vision-preview", AppConfig.Api.OPEN_AI_API_URL, conversation)
                            upsertNewMessage(regenerateMessage, modelId, chatId, answer)
                        }
                        "audio" -> {
                            Napier.v("Resource is audio, transcribing and answering with text and audio")
                            val speechToTextContent = transcribeAudio("whisper-1", AppConfig.Api.OPEN_AI_API_URL, resource)
                            val modifiedConversation = conversation.toMutableList()
                            modifiedConversation.removeLast()
                            modifiedConversation.add(
                                lastMessage.toChatMessage(
                                    modelIds = listOf(modelId),
                                    previousMessageContent = "${modifiedConversation.last().content}\n$speechToTextContent",
                                    resources = resources.filter { it.type.startsWith("image") }
                                )
                            )
                            val answer = answerChatWithText(modelId, modelEndpoint, modifiedConversation)
                            val audioResource = generateSpeechFromText("tts-1", AppConfig.Api.OPEN_AI_API_URL, answer)
                            // Append transcription to last message
                            chatService.editMessage(
                                modelId,
                                lastMessage.id!!,
                                lastMessage.copy(message = "${lastMessage.message}\n$speechToTextContent")
                            )
                            // Upload resource
                            // Send message with resourceId
                            val newResourceId = chatService.createResource(modelId, audioResource).id!!
                            upsertNewMessage(regenerateMessage, modelId, chatId, answer, listOf(newResourceId))
                        }
                        else -> Napier.v { "Resource type ${resource.type} not supported" }
                    }
                }
            }
            awaitingChatReplies[modelId] = awaitingChatReplies[modelId]!!.filter { it != chatId }
        } else {
            throw Exception("Chat with id $chatId is already awaiting a reply")
        }
    }

    private suspend fun upsertNewMessage(
        regenerateMessage: MessageDto?,
        modelId: String,
        chatId: String,
        answer: String,
        resourceIds: List<String> = regenerateMessage?.resourceIds ?: emptyList()
    ) {
        try {
            val newMessage = MessageDto(
                id = regenerateMessage?.id,
                senderId = modelId,
                chatId = chatId,
                message = answer,
                time = regenerateMessage?.time ?: Clock.System.now().toEpochMilliseconds(),
                resourceIds = resourceIds
            )
            if (regenerateMessage != null) {
                chatService.editMessage(modelId, regenerateMessage.id!!, newMessage)
            } else {
                chatService.sendMessage(modelId, newMessage)
            }
        } catch (e: Exception) {
            Napier.e("Error sending message for model with id: $modelId and chat with id: $chatId", e)
        }
    }

    suspend fun answerChatWithText(modelId: String, endpoint: String, conversation: List<ChatMessage>): String {
        // generate response text
        val openAI = openAIClients[endpoint] ?: throw Exception("No OpenAI client found for endpoint $endpoint")
        return openAI.chatCompletion(
            request = ChatCompletionRequest(
                model = ModelId(modelId),
                messages = conversation
            )
        ).choices.first().message.content!!
    }

    suspend fun transcribeAudio(modelId: String, endpoint: String, resource: ResourceDto): String {
        // get chat history with chatId
        // transcribe audio with whisper or other speech to text model
        // answer the transcribed text
        val openAI = openAIClients[endpoint] ?: throw Exception("No OpenAI client found for endpoint $endpoint")
        val type = resource.type.split("/").last()
        // Create tmp file for audio
        val audioFilePath = kotlin.io.path.createTempFile("${resource.id}_audio_file", ".$type")
        return openAI.transcription(
            request = TranscriptionRequest(
                audio = FileSource(name = resource.id ?: "audio_file.$type", source = audioFilePath.source()),
                model = ModelId(modelId)
            )
        ).text.also { audioFilePath.toFile().delete() }
    }

    suspend fun generateSpeechFromText(modelId: String, endpoint: String, text: String): ResourceDto {
        // generate audio from text
        // return audio resource
        val openAI = openAIClients[endpoint] ?: throw Exception("No OpenAI client found for endpoint $endpoint")
        return ResourceDto(
            type = "audio/wav",
            data = openAI.speech(
                request = SpeechRequest(
                    model = ModelId(modelId),
                    input = text,
                    responseFormat = SpeechResponseFormat("wav")
                )
            )
        )
    }
}

fun List<MessageDto>.toConversation(modelIds: List<String>, resources: List<ResourceDto>): List<ChatMessage> {
    val currentMessages = this
    return fold<MessageDto, MutableList<ChatMessage>>(mutableListOf()) { acc, message ->
        val previousAndCurrentMessageFromAssistant = acc.lastOrNull()?.role == ChatRole.Assistant && modelIds.contains(message.senderId)
        val previousAndCurrentMessageFromUser = acc.lastOrNull()?.role == ChatRole.User && !modelIds.contains(message.senderId)
        if (previousAndCurrentMessageFromAssistant || previousAndCurrentMessageFromUser) {
            val previousMessageContent = acc.last().content
            acc.removeLast()
            acc.add(message.toChatMessage(modelIds, previousMessageContent, resources))
            return@fold acc
        }
        acc.add(message.toChatMessage(modelIds, resources = resources))
        acc
    }.also {
        Napier.v { "Converted messages $currentMessages to conversation: $it" }
    }
}

@OptIn(ExperimentalEncodingApi::class)
fun MessageDto.toChatMessage(
    modelIds: List<String>,
    previousMessageContent: String? = null,
    resources: List<ResourceDto> = emptyList(),
): ChatMessage {
    val textPart = if (previousMessageContent?.filter { it != '\n' }.isNullOrBlank()) message ?: "" else "$previousMessageContent\n$message"
    val content = mutableListOf<ContentPart>()
    resources.filter { it.type.startsWith("image") }.forEach { imageResource ->
        val imageData = "data:${imageResource.type};base64,${Base64.encode(imageResource.data)}"
        content += ImagePart(imageData)
    }
    if (textPart.isNotBlank()) content += TextPart(textPart)
    return if (resources.isEmpty()) {
        // Only text
        ChatMessage(
            role = if (modelIds.contains(senderId)) ChatRole.Assistant else ChatRole.User,
            messageContent = TextContent(textPart)
        )
    } else {
        // Image with text
        ChatMessage(
            role = if (modelIds.contains(senderId)) ChatRole.Assistant else ChatRole.User,
            content = content
        )
    }
}