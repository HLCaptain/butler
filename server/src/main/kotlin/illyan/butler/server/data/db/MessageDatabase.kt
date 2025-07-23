package illyan.butler.server.data.db

import illyan.butler.shared.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface MessageDatabase {
    suspend fun sendMessage(userId: Uuid, message: MessageDto): MessageDto
    suspend fun editMessage(userId: Uuid, message: MessageDto): MessageDto
    suspend fun deleteMessage(userId: Uuid, chatId: Uuid, messageId: Uuid): Boolean
    suspend fun getPreviousMessages(userId: Uuid, chatId: Uuid, limit: Int, timestamp: Long): List<MessageDto>
    fun getPreviousMessagesFlow(userId: Uuid, chatId: Uuid, limit: Int, timestamp: Long): Flow<List<MessageDto>>
    suspend fun getMessages(userId: Uuid, chatId: Uuid, limit: Int, offset: Int): List<MessageDto>
    fun getMessagesFlow(userId: Uuid, chatId: Uuid, limit: Int, offset: Int): Flow<List<MessageDto>>
    suspend fun getMessages(userId: Uuid, chatId: Uuid): List<MessageDto>
    fun getMessagesFlow(userId: Uuid, chatId: Uuid): Flow<List<MessageDto>>
    suspend fun getMessages(userId: Uuid): List<MessageDto>
    fun getMessagesFlow(userId: Uuid): Flow<List<MessageDto>>
    fun getChangedMessagesAffectingUser(userId: Uuid): Flow<List<MessageDto>>
    fun getChangedMessagesAffectingChat(userId: Uuid, chatId: Uuid): Flow<List<MessageDto>>
}
