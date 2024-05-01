package illyan.butler.services.ai.data.service

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import illyan.butler.services.ai.AppConfig
import illyan.butler.services.ai.data.model.ai.ModelDto
import illyan.butler.services.ai.data.model.chat.ChatDto
import illyan.butler.services.ai.data.model.chat.MessageDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class LlmService(
    private val modelHealthService: ModelHealthService,
    private val chatService: ChatService,
    private val openAIClients: Map<String, OpenAI>,
    private val coroutineScope: CoroutineScope
) {
    // All messages per model
    // Messages can be grouped and sorted by chatId
    private val chatsByModels = hashMapOf<String, List<ChatDto>>()
    private val awaitingChatReplies = hashMapOf<String, List<String>>()
    private var modelsAndProviderIds = mapOf<ModelDto, List<String>>()

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
                                Napier.v("Fetched ${chats.size} chats for model with id: ${model.id}")
                                emit(chatsByModels[model.id]!!.mapNotNull { it.id }.toSet())
                                delay(10000L)
                            }
                        }
                    }
                }.merge()
            }.collectLatest { chatIds -> updateChatsIfNeeded(chatIds) }
        }
    }

    private suspend fun updateChatsIfNeeded(chatIds: Set<String>) = chatIds.forEach { sendMessageIfNeeded(it) }

    private suspend fun sendMessageIfNeeded(chatId: String) {
        val modelId = chatsByModels.keys.find { modelId -> chatsByModels[modelId]?.any { it.id == chatId } == true }
        val chat = chatsByModels[modelId]?.firstOrNull { it.id == chatId }
        val messages = chat?.lastFewMessages?.sortedBy { it.time } ?: return
        if (messages.isEmpty() || messages.last().senderId == modelId) return
        // Then last message is by a user, so run inference on it
        val chatMessages = messages.toConversation(listOf(modelId!!))
        val modelEndpoint = chat.aiEndpoints[modelId] ?: AppConfig.Api.LOCAL_AI_OPEN_AI_API_URL // Use local AI (self-hosting) as default
        val openAI = openAIClients[modelEndpoint] ?: throw Exception("No OpenAI client found for endpoint $modelEndpoint")
        val chatCompletion = openAI.chatCompletion(
            request = ChatCompletionRequest(
                model = ModelId(modelId),
                messages = chatMessages
            )
        )
        if (awaitingChatReplies[modelId]?.contains(chatId) != true) {
            awaitingChatReplies[modelId] = ((awaitingChatReplies[modelId] ?: emptyList()) + chatId).distinct()
            try {
                chatService.sendMessage(
                    modelId,
                    MessageDto(
                        senderId = modelId,
                        chatId = chatId,
                        message = chatCompletion.choices.first().message.content!!,
                        time = Clock.System.now().toEpochMilliseconds()
                    )
                )
            } catch (e: Exception) {
                Napier.e("Error sending message for model with id: $modelId and chat with id: $chatId", e)
            } finally {
                awaitingChatReplies[modelId] = awaitingChatReplies[modelId]!!.filter { it != chatId }
            }
        }
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
            val messages = chat?.lastFewMessages?.sortedBy { it.time } ?: return
            val chatMessages = messages.toConversation(listOf(modelId!!))
            val modelEndpoint = chat.aiEndpoints[modelId] ?: AppConfig.Api.LOCAL_AI_OPEN_AI_API_URL // Use local AI (self-hosting) as default
            val openAI = openAIClients[modelEndpoint] ?: throw Exception("No OpenAI client found for endpoint $modelEndpoint")
            val updatedMessage = openAI.chatCompletion(
                request = ChatCompletionRequest(
                    model = ModelId(modelId),
                    messages = chatMessages
                )
            ).choices.first().message.content!!
            if (awaitingChatReplies[modelId]?.contains(chatId) != true) {
                awaitingChatReplies[modelId] = ((awaitingChatReplies[modelId] ?: emptyList()) + chatId).distinct()
                try {
                    chatService.editMessage(
                        modelId,
                        messageId,
                        MessageDto(
                            id = messageId,
                            senderId = modelId,
                            chatId = chatId,
                            message = updatedMessage,
                            time = Clock.System.now().toEpochMilliseconds()
                        )
                    )
                } catch (e: Exception) {
                    Napier.e("Error sending message for model with id: $modelId and chat with id: $chatId", e)
                } finally {
                    awaitingChatReplies[modelId] = awaitingChatReplies[modelId]!!.filter { it != chatId }
                }
            }
        } else {
            // Add new message
            sendMessageIfNeeded(chatId)
        }
    }
}

fun List<MessageDto>.toConversation(modelIds: List<String>): List<ChatMessage> {
    val currentMessages = this
    return fold<MessageDto, MutableList<ChatMessage>>(mutableListOf()) { acc, message ->
        val previousAndCurrentMessageFromAssistant = acc.lastOrNull()?.role == ChatRole.Assistant && modelIds.contains(message.senderId)
        val previousAndCurrentMessageFromUser = acc.lastOrNull()?.role == ChatRole.User && !modelIds.contains(message.senderId)
        if (previousAndCurrentMessageFromAssistant || previousAndCurrentMessageFromUser) {
            val previousMessageContent = acc.last().content
            acc.removeLast()
            acc.add(message.toChatMessage(modelIds, previousMessageContent))
            return@fold acc
        }
        acc.add(message.toChatMessage(modelIds))
        acc
    }.also {
        Napier.v { "Converted messages $currentMessages to conversation: $it" }
    }
}

fun MessageDto.toChatMessage(modelIds: List<String>, previousMessageContent: String? = null) = ChatMessage(
    role = if (modelIds.contains(senderId)) ChatRole.Assistant else ChatRole.User,
    content = if (previousMessageContent != null) "$previousMessageContent\n$message" else message
)