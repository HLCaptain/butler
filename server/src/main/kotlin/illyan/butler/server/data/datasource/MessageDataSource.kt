package illyan.butler.server.data.datasource

import illyan.butler.shared.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface MessageDataSource {
    suspend fun sendMessage(userId: Uuid, message: MessageDto): MessageDto
    suspend fun editMessage(userId: Uuid, message: MessageDto): MessageDto
    suspend fun deleteMessage(userId: Uuid, chatId: Uuid, messageId: Uuid): Boolean
    suspend fun getPreviousMessages(userId: Uuid, chatId: Uuid, limit: Int, timestamp: Long): List<MessageDto>
    suspend fun getMessages(userId: Uuid, chatId: Uuid, limit: Int, offset: Int): List<MessageDto>
    suspend fun getMessages(userId: Uuid, chatId: Uuid): List<MessageDto>
    suspend fun getMessages(userId: Uuid): List<MessageDto>
    fun getChangedMessagesByUser(userId: Uuid): Flow<List<MessageDto>>
    fun getChangedMessagesByChat(userId: Uuid, chatId: Uuid): Flow<List<MessageDto>>
}
