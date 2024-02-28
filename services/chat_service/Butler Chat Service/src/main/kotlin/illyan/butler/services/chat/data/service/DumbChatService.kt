package illyan.butler.services.chat.data.service

import illyan.butler.services.chat.data.datasource.ChatDataSource
import illyan.butler.services.chat.data.datasource.MessageDataSource
import illyan.butler.services.chat.data.db.ChatDatabase
import illyan.butler.services.chat.data.db.MessageDatabase
import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

/**
 * Dummy implementation of the chat service.
 * Uses memory only database, which is initialized with predefined data.
 */
@Single
class DumbChatService(
    private val chatDatabase: ChatDatabase,
    private val messageDatabase: MessageDatabase,
    private val dispatcher: CoroutineDispatcher,
    private val coroutineScope: CoroutineScope
): ChatDataSource, MessageDataSource {

    init {
        coroutineScope.launch(dispatcher) {
            chatDatabase.createChat("user1", ChatDto("chat1", "Fun Chat", listOf("user1", "user2")))
            chatDatabase.createChat("user2", ChatDto("chat2", "Work Chat", listOf("user2", "user3")))

            messageDatabase.sendMessage("user1", MessageDto("msg1", "user1", "Hello in Fun Chat!", listOf(), System.currentTimeMillis(), "chat1"))
            messageDatabase.sendMessage("user2", MessageDto("msg2", "user2", "Hello in Work Chat!", listOf(), System.currentTimeMillis(), "chat2"))
        }
    }
    override suspend fun getChat(userId: String, chatId: String): ChatDto {
        return withContext(dispatcher) {
            chatDatabase.getChat(userId, chatId)
        }
    }

    override suspend fun createChat(userId: String, chat: ChatDto): ChatDto {
        return withContext(dispatcher) {
            chatDatabase.createChat(userId, chat)
        }
    }

    override suspend fun editChat(userId: String, chat: ChatDto) {
        withContext(dispatcher) {
            chatDatabase.editChat(userId, chat)
        }
    }

    override suspend fun deleteChat(userId: String, chatId: String): Boolean {
        return withContext(dispatcher) {
            chatDatabase.deleteChat(userId, chatId)
        }
    }

    override suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return withContext(dispatcher) {
            chatDatabase.getChats(userId, limit, offset)
        }
    }

    override suspend fun getChats(userId: String, fromDate: Long, toDate: Long): List<ChatDto> {
        return withContext(dispatcher) {
            chatDatabase.getChats(userId, fromDate, toDate)
        }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto> {
        return withContext(dispatcher) {
            chatDatabase.getPreviousChats(userId, limit, timestamp)
        }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return withContext(dispatcher) {
            chatDatabase.getPreviousChats(userId, limit, offset)
        }
    }

    override fun getChangedChatsAffectingUser(userId: String): Flow<ChatDto> {
        return emptyFlow()
    }

    override fun getChangesFromChat(chatId: String): Flow<ChatDto> {
        return emptyFlow()
    }

    override suspend fun sendMessage(userId: String, message: MessageDto) {
        withContext(dispatcher) {
            messageDatabase.sendMessage(userId, message)
        }
    }

    override suspend fun editMessage(userId: String, message: MessageDto) {
        withContext(dispatcher) {
            messageDatabase.editMessage(userId, message)
        }
    }

    override suspend fun deleteMessage(userId: String, chatId: String, messageId: String): Boolean {
        return withContext(dispatcher) {
            messageDatabase.deleteMessage(userId, chatId, messageId)
        }
    }

    override suspend fun getPreviousMessages(
        userId: String,
        chatId: String,
        limit: Int,
        timestamp: Long
    ): List<MessageDto> {
        return withContext(dispatcher) {
            messageDatabase.getPreviousMessages(userId, chatId, limit, timestamp)
        }
    }

    override suspend fun getMessages(userId: String, chatId: String, limit: Int, offset: Int): List<MessageDto> {
        return withContext(dispatcher) {
            messageDatabase.getMessages(userId, chatId, limit, offset)
        }
    }

    override fun getChangedMessagesByUser(userId: String): Flow<List<MessageDto>> {
        return emptyFlow()
    }

    override fun getChangedMessagesByChat(chatId: String): Flow<List<MessageDto>> {
        return emptyFlow()
    }
}