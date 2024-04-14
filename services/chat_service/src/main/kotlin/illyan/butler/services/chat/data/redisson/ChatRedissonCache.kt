package illyan.butler.services.chat.data.redisson

import illyan.butler.services.chat.data.cache.ChatCache
import illyan.butler.services.chat.data.model.chat.ChatDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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
class ChatRedissonCache(
    private val client: RedissonClient,
    private val dispatcher: CoroutineDispatcher
) : ChatCache {
    override suspend fun getChat(chatId: String): ChatDto? {
        return withContext(dispatcher) {
            client.getBucket<ChatDto?>("chat:$chatId").get()
        }
    }

    override suspend fun getChatsByUser(userId: String): List<ChatDto> {
        return withContext(dispatcher) {
            val chatIds = client.getSet<String>("user:$userId:chats").readAll()
            val chats = chatIds.mapNotNull { client.getBucket<ChatDto?>("chat:$it").get() }
            chats
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getChangedChatsAffectingUser(userId: String): Flow<List<ChatDto>> = callbackFlow {
        // Subscribe to each Chat the user is member of
        val topic = client.getTopic("user:$userId:chats")
        Napier.v("Subscribed to topic: user:$userId:chats")
        val userChatIds = MutableStateFlow(emptyList<String>())
        val listenerId = topic.addListener(List::class.java) { channel, msg ->
            try {
                Napier.v("Received message on topic, updating userChatIds")
                userChatIds.update { msg as List<String> }
            } catch (e: Exception) {
                // Handle exception, possibly closing the flow if needed
                Napier.e("Exception occurred while updating userChatIds", e)
                close(e)
            }
        }

        userChatIds.flatMapLatest { chatIds ->
            Napier.v("Processing chatIds from userChatIds")
            combine(chatIds.map { chatId -> getChangesFromChat(chatId) }) { it.toList() }
        }.collectLatest { chats ->
            try {
                // Send the chat to the flow
                Napier.v("Sending chats to the flow")
                this.trySend(chats).isSuccess
            } catch (e: Exception) {
                // Handle exception, possibly closing the flow if needed
                Napier.e("Exception occurred while sending chats to the flow", e)
                close(e)
            }
        }

        // Await close, which unsubscribes the listener on flow cancellation
        awaitClose {
            Napier.v("Unsubscribing listener from topic")
            topic.removeListener(listenerId)
        }
    }.flowOn(dispatcher)

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
    }.flowOn(dispatcher)

    override suspend fun setChat(chat: ChatDto): ChatDto {
        Napier.v { "Setting chat in cache: $chat" }
        return withContext(dispatcher) {
            val transaction = client.createTransaction(TransactionOptions.defaults()).also { transaction ->
                Napier.v { "Created transaction" }
                if (client.buckets.get<ChatDto>("chat:${chat.id}").isNotEmpty()) {
                    Napier.v { "Chat already exists in cache" }
                    val oldChat = transaction.getBucket<ChatDto>("chat:${chat.id}").get()
                    Napier.v { "Fetched old chat: $oldChat" }
                    transaction.getBucket<ChatDto>("chat:${chat.id}").set(chat)
                    // Update users' member status
                    val addedUsers = chat.members.filter { !oldChat.members.contains(it) }
                    val removedUsers = oldChat.members.filter { !chat.members.contains(it) }
                    Napier.v { "Added users: $addedUsers, Removed users: $removedUsers" }
                    addedUsers.forEach { member ->
                        Napier.v { "Processing added user: $member" }
                        transaction.getSet<ChatDto>("user:$member:chats").add(chat)
                        val currentChats = transaction.getSet<ChatDto>("user:$member:chats").readAll()
                        Napier.v { "Current chats for added user: $currentChats" }
                        client.getTopic("user:$member:chats").publish((currentChats + chat).distinctBy { it.id })
                    }
                    removedUsers.forEach { member ->
                        Napier.v { "Processing removed user: $member" }
                        transaction.getSet<ChatDto>("user:$member:chats").removeIf { it.id == chat.id }
                        val currentChats = transaction.getSet<ChatDto>("user:$member:chats").readAll()
                        Napier.v { "Current chats for removed user: $currentChats" }
                        client.getTopic("user:$member:chats").publish((currentChats - chat).distinctBy { it.id })
                    }
                } else {
                    Napier.v { "Chat does not exist in cache" }
                    transaction.getBucket<ChatDto>("chat:${chat.id}").set(chat)
                    chat.members.forEach { member ->
                        transaction.getSet<ChatDto>("user:$member:chats").add(chat)
                        val currentChats = transaction.getSet<ChatDto>("user:$member:chats").readAll()
                        Napier.v { "Current chats for removed user: $currentChats" }
                        client.getTopic("user:$member:chats").publish((currentChats + chat).distinctBy { it.id })
                    }
                }
                client.getTopic("chat:${chat.id}").publish(chat)
                Napier.v { "Published chat to topic" }
            }
            try {
                transaction.commit()
            } catch (e: TransactionException) {
                Napier.e("Error setting chat in cache, rollback transaction", e)
                transaction.rollback()
            }
            chat
        }
    }

    override suspend fun deleteChat(chatId: String): Boolean {
        return withContext(Dispatchers.IO) {
            val isDeleted: Boolean
            val transaction = client.createTransaction(TransactionOptions.defaults()).also { transaction ->
                val chatMembers = transaction.getSet<String>("chat:$chatId:members").readAll()
                isDeleted = transaction.getBucket<ChatDto>("chat:$chatId").delete()
                chatMembers.forEach { member ->
                    transaction.getSet<ChatDto>("user:$member:chats").removeIf { it.id == chatId }
                    val currentChats = transaction.getSet<ChatDto>("user:$member:chats").readAll()
                    client.getTopic("user:$member:chats").publish(currentChats)
                }
                client.getTopic("chat:$chatId").publish(null)
            }
            try {
                transaction.commit()
            } catch (e: TransactionException) {
                Napier.e("Error deleting chat from cache, rollback transaction", e)
                transaction.rollback()
            }
            isDeleted
        }
    }
}