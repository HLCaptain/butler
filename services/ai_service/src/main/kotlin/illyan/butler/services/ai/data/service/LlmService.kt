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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
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
    private val messagesByModels = hashMapOf<String, List<MessageDto>>()

    private val awaitingChatReplies = hashMapOf<String, List<String>>()

    fun loadModels() {
        coroutineScope.launch {
            modelHealthService.healthyModels.filterNotNull().first().let { models ->
                // Chat service api endpoint: get message flow for user
                // Chat service api endpoint: get chat with all messages

                // If model is not in chatsByModels, add it and get all chats for the model and get all messages for each chat and receive messages for model
                models.forEach { model ->
                    Napier.v("Processing model with id: ${model.id}")
                    if (!messagesByModels.containsKey(model.id)) {
                        Napier.v("Model with id: ${model.id} not found in chatsByModels. Fetching chats.")
                        coroutineScope.launch { // FIXME: fix websocket then remove pulling
                            while (true) {
                                val chats = try {
                                    chatService.getChats(model.id)
                                } catch (e: Exception) {
                                    Napier.e("Error fetching chats for model with id: ${model.id}", e)
                                    emptyList()
                                }
                                messagesByModels[model.id] = chats.fold(emptyList()) { acc, chatDto -> acc + chatDto.lastFewMessages }
                                Napier.v("Fetched ${chats.size} chats for model with id: ${model.id}")
                                updateChatsIfNeeded(messagesByModels[model.id]!!.map { it.chatId }.toSet())
                                delay(1000L)
                            }
                        }
//                        coroutineScope.launch {
//                            chatService.receiveMessages(model.id).collectLatest { messages ->
//                                Napier.v("Received ${messages.size} messages for model with id: ${model.id}")
//                                messagesByModels[model.id] = (messagesByModels[model.id]!! + messages).distinctBy { it.id }
//                                updateChatsIfNeeded(messages.map { it.chatId }.toSet())
//                            }
//                        }
                    }
                }
            }
        }
    }

    private suspend fun updateChatsIfNeeded(chatIds: Set<String>) = chatIds.forEach { sendMessageIfNeeded(it) }

    private suspend fun sendMessageIfNeeded(chatId: String) {
        val modelId = messagesByModels.keys.find { modelId -> messagesByModels[modelId]?.any { it.chatId == chatId } == true }
        val messages = messagesByModels[modelId]?.filter { it.chatId == chatId }?.sortedBy { it.time } ?: return
        if (messages.isEmpty() || messages.last().senderId == modelId) return
        // Then last message is by a user, so run inference on it
        val chatMessages = messages.toConversation(listOf(modelId!!))
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
            val modelId = messagesByModels.keys.find { modelId -> messagesByModels[modelId]?.any { it.chatId == chatId } == true }
            val messages = messagesByModels[modelId]?.filter { it.chatId == chatId }?.sortedBy { it.time }?.takeWhile { it.id != messageId } ?: return
            val chatMessages = messages.toConversation(listOf(modelId!!))
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

fun List<MessageDto>.toConversation(modelIds: List<String>): List<ChatMessage> = foldRight(mutableListOf()) { message, acc ->
    val previousAndCurrentMessageFromAssistant = acc.lastOrNull()?.role == ChatRole.Assistant && modelIds.contains(message.senderId)
    val previousAndCurrentMessageFromUser = acc.lastOrNull()?.role == ChatRole.User && !modelIds.contains(message.senderId)
    if (previousAndCurrentMessageFromAssistant || previousAndCurrentMessageFromUser) {
        val previousMessageContent = acc.last().content
        acc.removeLast()
        acc.add(message.toChatMessage(modelIds, previousMessageContent))
        return@foldRight acc
    }
    acc.add(message.toChatMessage(modelIds))
    acc
}

fun MessageDto.toChatMessage(modelIds: List<String>, previousMessageContent: String? = null) = ChatMessage(
    role = if (modelIds.contains(senderId)) ChatRole.Assistant else ChatRole.User,
    content = if (previousMessageContent != null) "$previousMessageContent\n$message" else message
)