package illyan.butler.services.chat.data.service

import illyan.butler.services.chat.data.datasource.ChatDataSource
import illyan.butler.services.chat.data.datasource.MessageDataSource
import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow

/**
 * Dummy implementation of the chat service.
 * Uses memory only database, which is initialized with predefined data.
 */
class DumbChatService: ChatDataSource, MessageDataSource {
    override suspend fun getChat(userId: String, chatId: String): ChatDto {
        TODO("Not yet implemented")
    }

    override suspend fun createChat(userId: String, chat: ChatDto): ChatDto {
        TODO("Not yet implemented")
    }

    override suspend fun editChat(userId: String, chat: ChatDto) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteChat(userId: String, chatId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getChats(userId: String, fromDate: Long, toDate: Long): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override fun getChangedChatsAffectingUser(userId: String): Flow<ChatDto> {
        TODO("Not yet implemented")
    }

    override fun getChangesFromChat(chatId: String): Flow<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun sendMessage(userId: String, message: MessageDto) {
        TODO("Not yet implemented")
    }

    override suspend fun editMessage(userId: String, message: MessageDto) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(userId: String, chatId: String, messageId: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun getPreviousMessages(
        userId: String,
        chatId: String,
        limit: Int,
        timestamp: Long
    ): List<MessageDto> {
        TODO("Not yet implemented")
    }

    override suspend fun getMessages(userId: String, chatId: String, limit: Int, offset: Int): List<MessageDto> {
        TODO("Not yet implemented")
    }

    override fun getChangedMessagesByUser(userId: String): Flow<List<MessageDto>> {
        TODO("Not yet implemented")
    }

    override fun getChangedMessagesByChat(chatId: String): Flow<List<MessageDto>> {
        TODO("Not yet implemented")
    }
}