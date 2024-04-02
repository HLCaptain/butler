package illyan.butler.services.chat.data.redisson

import illyan.butler.services.chat.data.cache.MessageCache
import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
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
class MessageRedissonCache(
    private val client: RedissonClient,
    private val dispatcher: CoroutineDispatcher
) : MessageCache {
    override suspend fun getMessage(messageId: String): MessageDto? {
        return withContext(dispatcher) {
            client.getBucket<MessageDto?>("message:$messageId").async.get()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getChangedMessagesByUser(userId: String): Flow<List<MessageDto>> {
        return callbackFlow {
            val topic = client.getTopic("user:$userId:chats")
            val userChats = MutableStateFlow(emptyList<ChatDto>())
            val listenerId = topic.addListenerAsync(List::class.java) { channel, msg ->
                try {
                    userChats.update { msg as List<ChatDto> }
                } catch (e: Exception) {
                    close(e)
                }
            }.get()

            userChats.flatMapLatest { chats ->
                chats.map { chat -> getChangedMessagesByChat(chat.id!!) }.merge()
            }.collectLatest { messages ->
                try {
                    trySend(messages).isSuccess
                } catch (e: Exception) {
                    close(e)
                }
            }

            awaitClose { topic.removeListenerAsync(listenerId).get() }
        }.flowOn(dispatcher)
    }

    override fun getChangedMessagesByChat(chatId: String): Flow<List<MessageDto>> {
        return callbackFlow {
            val topic = client.getTopic("chat:$chatId:messages")
            val chatMessages = MutableStateFlow(emptyList<MessageDto>())
            val listenerId = topic.addListenerAsync(List::class.java) { channel, msg ->
                try {
                    chatMessages.update { msg as List<MessageDto> }
                } catch (e: Exception) {
                    close(e)
                }
            }.get()

            chatMessages.collectLatest { messages ->
                try {
                    trySend(messages).isSuccess
                } catch (e: Exception) {
                    close(e)
                }
            }

            awaitClose { topic.removeListenerAsync(listenerId).get() }
        }.flowOn(dispatcher)
    }

    override suspend fun setMessage(message: MessageDto): MessageDto {
        return withContext(dispatcher) {
            client.createBatch().let { batch ->
                batch.getBucket<MessageDto>("message:${message.id}").setAsync(message).get()
                batch.getTopic("chat:${message.chatId}:messages").publishAsync(message).get()
                batch.executeAsync().get()
            }
            message
        }
    }

    override suspend fun deleteMessage(messageId: String): Boolean {
        return withContext(dispatcher) {
            client.getBucket<MessageDto>("message:$messageId").deleteAsync().get()
        }
    }
}