package illyan.butler.server.data.datasource

import illyan.butler.shared.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessageDataSource {
    suspend fun sendMessage(userId: String, message: MessageDto): MessageDto
    suspend fun editMessage(userId: String, message: MessageDto): MessageDto
    suspend fun deleteMessage(userId: String, chatId: String, messageId: String): Boolean
    suspend fun getPreviousMessages(userId: String, chatId: String, limit: Int, timestamp: Long): List<MessageDto>
    suspend fun getMessages(userId: String, chatId: String, limit: Int, offset: Int): List<MessageDto>
    suspend fun getMessages(userId: String, chatId: String): List<MessageDto>
    suspend fun getMessages(userId: String): List<MessageDto>
    fun getChangedMessagesByUser(userId: String): Flow<List<MessageDto>>
    fun getChangedMessagesByChat(userId: String, chatId: String): Flow<List<MessageDto>>
}