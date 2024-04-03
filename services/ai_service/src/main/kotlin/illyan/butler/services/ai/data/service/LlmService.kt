package illyan.butler.services.ai.data.service

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import illyan.butler.services.ai.data.model.chat.MessageDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class LlmService(
    private val modelHealthService: ModelHealthService,
    private val chatService: ChatService,
    private val client: HttpClient,
    private val openAI: OpenAI, // Only the Local AI OpenAI client is used
    private val coroutineScope: CoroutineScope
) {
    // All messages per model
    // Messages can be grouped and sorted by chatId
    private val chatsByModels = hashMapOf<String, List<MessageDto>>()

    fun loadModels() {
        coroutineScope.launch {
            modelHealthService.healthyModels.filterNotNull().collectLatest { models ->
                // Chat service api endpoint: get message flow for user
                // Chat service api endpoint: get chat with all messages

                // If model is not in chatsByModels, add it and get all chats for the model and get all messages for each chat and receive messages for model
                models.forEach { model ->
                    Napier.v("Processing model with id: ${model.id}")
                    if (!chatsByModels.containsKey(model.id)) {
                        Napier.v("Model with id: ${model.id} not found in chatsByModels. Fetching chats.")
                        val chats = chatService.getChats(model.id)
                        chatsByModels[model.id] = chats.fold(emptyList()) { acc, chatDto -> acc + chatDto.lastFewMessages }
                        Napier.v("Fetched ${chats.size} chats for model with id: ${model.id}")
                        coroutineScope.launch {
                            chatService.receiveMessages(model.id).collectLatest { messages ->
                                Napier.v("Received ${messages.size} messages for model with id: ${model.id}")
                                val messagesPerChat = messages.filter { it.chatId != null }.groupBy { it.chatId }
                                messagesPerChat.forEach { (chatId, chatMessages) ->
                                    chatsByModels[chatId!!] = chatsByModels[chatId]!! + chatMessages
                                    Napier.v("Added ${chatMessages.size} messages to chat with id: $chatId")
                                    // FIXME: take updated messages into account (update message with same ID with the updated version)
                                }
                                updateChatsIfNeeded(messagesPerChat.keys.filterNotNull().toSet())
                            }
                        }
                    }
                }
            }
        }
    }

    private suspend fun updateChatsIfNeeded(keys: Set<String>) = keys.forEach { updateChatIfNeeded(it) }

    private suspend fun updateChatIfNeeded(chatId: String) {
        val modelId = chatsByModels.keys.find { modelId -> chatsByModels[modelId]?.any { it.chatId == chatId } == true }
        val messages = chatsByModels[modelId]?.filter { it.chatId == chatId }?.sortedBy { it.time } ?: return
        if (messages.isEmpty() || messages.last().senderId == modelId) return
        // Then last message is by a user, so run inference on it
        val chatMessages = messages.map {
            ChatMessage(
                role = if (it.senderId == modelId) ChatRole.Assistant else ChatRole.User,
                content = it.message
            )
        }
        val chatCompletion = openAI.chatCompletion(
            request = ChatCompletionRequest(
                model = ModelId(modelId!!),
                messages = chatMessages
            )
        )
        chatService.sendMessage(
            modelId,
            MessageDto(
                senderId = modelId,
                chatId = chatId,
                message = chatCompletion.choices.first().message.content!!,
                time = Clock.System.now().toEpochMilliseconds()
            )
        )
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
            val modelId = chatsByModels.keys.find { modelId -> chatsByModels[modelId]?.any { it.chatId == chatId } == true }
            val messages = chatsByModels[modelId]?.filter { it.chatId == chatId }?.sortedBy { it.time }?.takeWhile { it.id != messageId } ?: return
            val chatMessages = messages.map {
                ChatMessage(
                    role = if (it.senderId == modelId) ChatRole.Assistant else ChatRole.User,
                    content = it.message
                )
            }
            val updatedMessage = openAI.chatCompletion(
                request = ChatCompletionRequest(
                    model = ModelId(modelId!!),
                    messages = chatMessages
                )
            ).choices.first().message.content!!
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
        } else {
            // Add new message
            updateChatIfNeeded(chatId)
        }
    }
}