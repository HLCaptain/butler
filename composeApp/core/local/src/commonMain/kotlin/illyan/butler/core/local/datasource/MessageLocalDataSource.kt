package illyan.butler.core.local.datasource

import illyan.butler.domain.model.Message
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface MessageLocalDataSource {
    suspend fun insertMessage(message: Message)
    suspend fun insertMessages(messages: List<Message>)
    suspend fun upsertMessage(message: Message)
    suspend fun replaceMessage(oldMessageId: Uuid, newMessage: Message)
    suspend fun deleteMessageById(messageId: Uuid)
    suspend fun deleteAllMessages()
    suspend fun deleteAllMessagesForChat(chatId: Uuid)
    fun getMessageById(messageId: Uuid): Flow<Message?>
    fun getMessagesByChatId(chatId: Uuid): Flow<List<Message>>
    fun getAccessibleMessagesForUser(userId: Uuid): Flow<List<Message>>
    suspend fun upsertMessages(newMessages: List<Message>)
}