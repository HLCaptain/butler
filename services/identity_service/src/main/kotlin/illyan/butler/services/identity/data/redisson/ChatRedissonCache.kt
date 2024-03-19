package illyan.butler.services.identity.data.redisson

import illyan.butler.services.identity.data.cache.ChatCache
import illyan.butler.services.identity.data.model.chat.ChatDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import org.redisson.api.RedissonClient

@Single
class ChatRedissonCache(
    private val client: RedissonClient,
    private val dispatcher: CoroutineDispatcher
) : ChatCache {
    override suspend fun getChat(chatId: String): ChatDto? {
        return withContext(dispatcher) {
            client.getBucket<ChatDto?>("chat:$chatId").async.get()
        }
    }

    override suspend fun getChatsByUser(userId: String): List<ChatDto> {
        return withContext(dispatcher) {
            client.createBatch().let { batch ->
                val chatIds = batch.getList<String>("user:$userId:chats").readAllAsync().get()
                val chats = chatIds.mapNotNull { batch.getBucket<ChatDto?>("chat:$it").async.get() }
                batch.executeAsync().get()
                chats
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getChangedChatsAffectingUser(userId: String) = callbackFlow {
        // Subscribe to each Chat the user is member of
        val topic = client.getTopic("user:$userId:chats")
        val userChatIds = MutableStateFlow(emptyList<String>())
        val listenerId = topic.addListenerAsync(List::class.java) { channel, msg ->
            try {
                userChatIds.update { msg as List<String> }
            } catch (e: Exception) {
                // Handle exception, possibly closing the flow if needed
                close(e)
            }
        }.get()

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
        awaitClose { topic.removeListenerAsync(listenerId) }
    }.flowOn(dispatcher)

    override fun getChangesFromChat(chatId: String) = callbackFlow {
        val topic = client.getTopic("chat:$chatId")
        val listenerId = topic.addListenerAsync(ChatDto::class.java) { channel, msg ->
            try {
                this.trySend(msg).isSuccess
            } catch (e: Exception) {
                // Handle exception, possibly closing the flow if needed
                close(e)
            }
        }.get()

        // Await close, which unsubscribes the listener on flow cancellation
        awaitClose { topic.removeListenerAsync(listenerId) }
    }.flowOn(dispatcher)

    override suspend fun setChat(chat: ChatDto): ChatDto {
        return withContext(dispatcher) {
            client.createBatch().let { batch ->
                val oldChat = batch.getBucket<ChatDto>("chat:${chat.id}").async.get()
                batch.getBucket<ChatDto>("chat:${chat.id}").setAsync(chat)
                // Update users' member status
                val addedUsers = chat.members.filter { !oldChat.members.contains(it) }
                val removedUsers = oldChat.members.filter { !chat.members.contains(it) }
                addedUsers.forEach { member ->
                    batch.getList<String>("user:$member:chats").addAsync(chat.id)
                    val currentChats = batch.getList<String>("user:$member:chats").readAllAsync().get()
                    batch.getTopic("user:$member:chats").publishAsync((currentChats + chat.id).distinct())
                }
                removedUsers.forEach { member ->
                    batch.getList<String>("user:$member:chats").removeAsync(chat.id)
                    val currentChats = batch.getList<String>("user:$member:chats").readAllAsync().get()
                    batch.getTopic("user:$member:chats").publishAsync(currentChats - chat.id)
                }
                batch.getTopic("chat:${chat.id}").publishAsync(chat).get()
                batch.executeAsync().get()
            }
            chat
        }
    }

    override suspend fun deleteChat(chatId: String): Boolean {
        return withContext(Dispatchers.IO) {
            client.createBatch().let { batch ->
                val chatMembers = batch.getList<String>("chat:$chatId:members").readAllAsync().get()
                val isDeleted = batch.getBucket<ChatDto>("chat:$chatId").deleteAsync().get()
                chatMembers.forEach { member ->
                    batch.getList<String>("user:$member:chats").removeAsync(chatId)
                    val currentChats = batch.getList<String>("user:$member:chats").readAllAsync().get()
                    batch.getTopic("user:$member:chats").publishAsync(currentChats - chatId)
                }
                batch.getTopic("chat:$chatId").publishAsync(null).get()
                batch.executeAsync().get()
                isDeleted
            }
        }
    }
}