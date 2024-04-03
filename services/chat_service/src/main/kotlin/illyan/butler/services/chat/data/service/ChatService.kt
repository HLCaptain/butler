package illyan.butler.services.chat.data.service

import illyan.butler.services.chat.data.cache.ChatCache
import illyan.butler.services.chat.data.cache.MessageCache
import illyan.butler.services.chat.data.datasource.ChatDataSource
import illyan.butler.services.chat.data.datasource.MessageDataSource
import illyan.butler.services.chat.data.db.ChatDatabase
import illyan.butler.services.chat.data.db.MessageDatabase
import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

/**
 * Chat service implementation.
 * Pipeline:
 *  1. Check Cache
 *  2. Update/get resource from Database if needed
 *  3. Update Cache if needed
 *  4. Return resource/flow of resource
 * TODO: Implement heavier caching
 * FIXME: check authorization
 */
@Single
class ChatService(
    private val chatCache: ChatCache,
    private val chatDatabase: ChatDatabase,
    private val messageCache: MessageCache,
    private val messageDatabase: MessageDatabase
) : ChatDataSource, MessageDataSource {
    override suspend fun getChat(userId: String, chatId: String): ChatDto {
        return chatCache.getChat(chatId) ?: chatDatabase.getChat(userId, chatId).also {
            chatCache.setChat(it)
        }
    }

    override suspend fun createChat(userId: String, chat: ChatDto): ChatDto {
        Napier.d("User $userId creating chat $chat")
        return chatDatabase.createChat(userId, chat).also {
            chatCache.setChat(it)
        }
    }

    override suspend fun editChat(userId: String, chat: ChatDto) {
        chatDatabase.editChat(userId, chat)
        chatCache.setChat(chat)
    }

    override suspend fun deleteChat(userId: String, chatId: String): Boolean {
        return chatDatabase.deleteChat(userId, chatId).also {
            chatCache.deleteChat(chatId)
        }
    }

    override suspend fun getChats(userId: String): List<ChatDto> {
        return chatDatabase.getChats(userId).onEach { chatCache.setChat(it) }
    }

    override suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return chatDatabase.getChats(userId, limit, offset).onEach { chatCache.setChat(it) }
    }

    override suspend fun getChats(userId: String, fromDate: Long, toDate: Long): List<ChatDto> {
        return chatDatabase.getChats(userId, fromDate, toDate).onEach { chatCache.setChat(it) }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto> {
        return chatDatabase.getPreviousChats(userId, limit, timestamp).onEach { chatCache.setChat(it) }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return chatDatabase.getPreviousChats(userId, limit, offset).onEach { chatCache.setChat(it) }
    }

    override fun getChangedChatsAffectingUser(userId: String): Flow<ChatDto> {
        return chatCache.getChangedChatsAffectingUser(userId)
    }

    override fun getChangesFromChat(chatId: String): Flow<ChatDto> {
        return chatCache.getChangesFromChat(chatId)
    }

    override suspend fun sendMessage(userId: String, message: MessageDto): MessageDto {
        return messageDatabase.sendMessage(userId, message).also { messageCache.setMessage(message) }
    }

    override suspend fun editMessage(userId: String, message: MessageDto): MessageDto {
        return messageDatabase.editMessage(userId, message).also { messageCache.setMessage(message) }
    }

    override suspend fun deleteMessage(userId: String, chatId: String, messageId: String): Boolean {
        return messageDatabase.deleteMessage(userId, chatId, messageId).also {
            messageCache.deleteMessage(messageId)
        }
    }

    override suspend fun getPreviousMessages(
        userId: String,
        chatId: String,
        limit: Int,
        timestamp: Long
    ): List<MessageDto> {
        return messageDatabase.getPreviousMessages(userId, chatId, limit, timestamp).onEach { messageCache.setMessage(it) }
    }

    override suspend fun getMessages(userId: String, chatId: String, limit: Int, offset: Int): List<MessageDto> {
        return messageDatabase.getMessages(userId, chatId, limit, offset)
    }

    override suspend fun getMessages(userId: String, chatId: String): List<MessageDto> {
        return messageDatabase.getMessages(userId, chatId).also { messageCache.setMessages(it) }
    }

    override fun getChangedMessagesByUser(userId: String): Flow<List<MessageDto>> {
        return messageCache.getChangedMessagesByUser(userId)
    }

    override fun getChangedMessagesByChat(chatId: String): Flow<List<MessageDto>> {
        return messageCache.getChangedMessagesByChat(chatId)
    }
}