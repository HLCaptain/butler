package illyan.butler.backend.data.db

import illyan.butler.backend.data.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessageDatabase {
    suspend fun sendMessage(userId: String, message: MessageDto): MessageDto
    suspend fun editMessage(userId: String, message: MessageDto): MessageDto
    suspend fun deleteMessage(userId: String, chatId: String, messageId: String): Boolean
    suspend fun getPreviousMessages(userId: String, chatId: String, limit: Int, timestamp: Long): List<MessageDto>
    suspend fun getMessages(userId: String, chatId: String, limit: Int, offset: Int): List<MessageDto>
    suspend fun getMessages(userId: String, chatId: String): List<MessageDto>
    suspend fun getMessages(userId: String): List<MessageDto>
    fun getChangedMessagesAffectingUser(userId: String): Flow<List<MessageDto>>
    fun getChangedMessagesAffectingChat(userId: String, chatId: String): Flow<List<MessageDto>>
}