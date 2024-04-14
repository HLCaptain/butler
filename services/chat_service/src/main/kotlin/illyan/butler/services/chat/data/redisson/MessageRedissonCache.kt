package illyan.butler.services.chat.data.redisson

import illyan.butler.services.chat.data.cache.MessageCache
import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import io.github.aakira.napier.Napier
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
import org.redisson.api.TransactionOptions
import org.redisson.transaction.TransactionException

@Single
class MessageRedissonCache(
    private val client: RedissonClient,
    private val dispatcher: CoroutineDispatcher
) : MessageCache {
    override suspend fun getMessage(messageId: String): MessageDto? {
        return withContext(dispatcher) {
            client.getBucket<MessageDto?>("message:$messageId").get()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getChangedMessagesByUser(userId: String) = callbackFlow {
        val topic = client.getTopic("user:$userId:messages")
        Napier.v("Subscribed to topic: user:$userId:messages")
        val userChats = MutableStateFlow(emptyList<MessageDto>())
        val listenerId = topic.addListener(List::class.java) { channel, msg ->
            try {
                Napier.v("Received message on topic, updating userChats")
                userChats.update { msg as List<MessageDto> }
            } catch (e: Exception) {
                Napier.e("Exception occurred while updating userChats", e)
                close(e)
            }
        }

        userChats.flatMapLatest { chats ->
            Napier.v("Processing chats from userChats")
            chats.map { chat -> getChangedMessagesByChat(chat.id!!) }.merge()
        }.collectLatest { messages ->
            try {
                Napier.v("Sending messages to the flow")
                trySend(messages).isSuccess
            } catch (e: Exception) {
                Napier.e("Exception occurred while sending messages to the flow", e)
                close(e)
            }
        }

        awaitClose {
            Napier.v("Unsubscribing listener from topic")
            topic.removeListener(listenerId)
        }
    }.flowOn(dispatcher)

    override fun getChangedMessagesByChat(chatId: String) = callbackFlow {
        val topic = client.getTopic("chat:$chatId:messages")
        Napier.v("Subscribed to topic: chat:$chatId:messages")
        val chatMessages = MutableStateFlow(emptyList<MessageDto>())
        val listenerId = topic.addListener(List::class.java) { channel, msg ->
            try {
                Napier.v("Received message on topic, updating chatMessages")
                chatMessages.update { msg as List<MessageDto> }
            } catch (e: Exception) {
                Napier.e("Exception occurred while updating chatMessages", e)
                close(e)
            }
        }

        chatMessages.collectLatest { messages ->
            try {
                Napier.v("Sending messages to the flow")
                trySend(messages).isSuccess
            } catch (e: Exception) {
                Napier.e("Exception occurred while sending messages to the flow", e)
                close(e)
            }
        }

        awaitClose {
            Napier.v("Unsubscribing listener from topic")
            topic.removeListener(listenerId)
        }
    }.flowOn(dispatcher)

    override suspend fun getMessagesByChat(chatId: String): List<MessageDto> {
        return withContext(dispatcher) {
            client.getBucket<List<MessageDto>?>("chat:$chatId:messages").get() ?: emptyList()
        }.also { Napier.d { "${it.size} messages for chat $chatId with last 5 message being ${it.takeLast(5)}" } }
    }

    override suspend fun setMessages(messages: List<MessageDto>): List<MessageDto> {
        return withContext(dispatcher) {
            Napier.v("Setting messages in cache: $messages")
            val transaction = client.createTransaction(TransactionOptions.defaults()).also { transaction ->
                Napier.v("Created transaction")
                messages.groupBy { it.chatId }.forEach { (chatId, messages) ->
                    Napier.v("Processing messages for chat: $chatId")
                    messages.forEach { message ->
                        Napier.v("Setting message in transaction: ${message.id}")
                        transaction.getBucket<MessageDto>("message:${message.id}").set(message)
                    }
                    Napier.v("Publishing messages to topic: chat:$chatId:messages")
                    client.getTopic("chat:$chatId:messages").publish(messages)
                    val chat = transaction.getBucket<ChatDto>("chat:$chatId").get()
                    Napier.v("Notifying members of chat: $chatId")
                    chat.members.forEach { member ->
                        Napier.v("Publishing messages to topic: user:$member:messages")
                        client.getTopic("user:$member:messages").publish(messages)
                    }
                }
            }
            try {
                Napier.v("Committing transaction")
                transaction.commit()
            } catch (e: TransactionException) {
                Napier.e("Error setting messages in cache, rollback transaction", e)
                transaction.rollback()
            }
            Napier.v("Exiting setMessages function")
            messages
        }
    }

    override suspend fun deleteMessage(messageId: String): Boolean {
        return withContext(dispatcher) {
            client.getBucket<MessageDto>("message:$messageId").delete()
        }
    }
}