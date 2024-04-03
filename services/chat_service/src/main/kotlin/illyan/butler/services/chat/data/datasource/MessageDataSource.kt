package illyan.butler.services.chat.data.datasource

import illyan.butler.services.chat.data.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessageDataSource {
    suspend fun sendMessage(userId: String, message: MessageDto): MessageDto
    suspend fun editMessage(userId: String, message: MessageDto): MessageDto
    suspend fun deleteMessage(userId: String, chatId: String, messageId: String): Boolean
    suspend fun getPreviousMessages(userId: String, chatId: String, limit: Int, timestamp: Long): List<MessageDto>
    suspend fun getMessages(userId: String, chatId: String, limit: Int, offset: Int): List<MessageDto>
    suspend fun getMessages(userId: String, chatId: String): List<MessageDto>
    fun getChangedMessagesByUser(userId: String): Flow<List<MessageDto>>
    fun getChangedMessagesByChat(chatId: String): Flow<List<MessageDto>>
}