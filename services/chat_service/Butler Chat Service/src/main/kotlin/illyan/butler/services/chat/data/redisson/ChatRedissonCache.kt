package illyan.butler.services.chat.data.redisson

import illyan.butler.services.chat.data.cache.ChatCache
import illyan.butler.services.chat.data.model.chat.ChatDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient

@Single
class ChatRedissonCache(
    private val client: RedissonClient,
) : ChatCache {
    override suspend fun getChat(chatId: String): ChatDto? {
        return withContext(Dispatchers.IO) {
            client.getBucket<ChatDto?>("chat:$chatId").async.get()
        }
    }

    override suspend fun getChatsByUser(userId: String): List<ChatDto> {
        val chatIds = client.getList<String>("user:$userId:chats").readAll()
        return chatIds.mapNotNull { getChat(it) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getChangedChatsAffectingUser(userId: String) = callbackFlow {
        // Subscribe to each Chat the user is member of
        val topic = client.getTopic("user:$userId:chats")
        val userChatIds = MutableStateFlow(emptyList<String>())
        val listenerId = topic.addListener(List::class.java) { channel, msg ->
            try {
                userChatIds.update { msg as List<String> }
            } catch (e: Exception) {
                // Handle exception, possibly closing the flow if needed
                close(e)
            }
        }

        userChatIds.flatMapLatest { chatIds ->
            chatIds.map { chatId -> getChangesFromChat(chatId) }.merge()
        }.collectLatest { chat ->
            try {
                // Send the chat to the flow
                this.trySend(chat).isSuccess
            } catch (e: Exception) {
                // Handle exception, possibly closing the flow if needed
                close(e)
            }
        }

        // Await close, which unsubscribes the listener on flow cancellation
        awaitClose { topic.removeListener(listenerId) }
    }

    override fun getChangesFromChat(chatId: String) = callbackFlow {
        val topic = client.getTopic("chat:$chatId")
        val listenerId = topic.addListener(ChatDto::class.java) { channel, msg ->
            try {
                this.trySend(msg).isSuccess
            } catch (e: Exception) {
                // Handle exception, possibly closing the flow if needed
                close(e)
            }
        }

        // Await close, which unsubscribes the listener on flow cancellation
        awaitClose { topic.removeListener(listenerId) }

    }

    override suspend fun setChat(chat: ChatDto): ChatDto {
        client.getBucket<ChatDto>("chat:${chat.id}").set(chat)
        return chat
    }

    override suspend fun deleteChat(chatId: String) {
        client.getBucket<ChatDto>("chat:$chatId").delete()
    }
}