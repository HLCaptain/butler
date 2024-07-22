package illyan.butler.data.local.datasource

import illyan.butler.domain.model.DomainMessage
import kotlinx.coroutines.flow.Flow

interface MessageLocalDataSource {
    suspend fun insertMessage(message: DomainMessage)
    suspend fun insertMessages(messages: List<DomainMessage>)
    suspend fun upsertMessage(message: DomainMessage)
    suspend fun replaceMessage(oldMessageId: String, newMessage: DomainMessage)
    suspend fun deleteMessageById(messageId: String)
    suspend fun deleteAllMessages()
    suspend fun deleteAllMessagesForChat(chatId: String)
    fun getMessageById(messageId: String): Flow<DomainMessage?>
    fun getMessagesByChatId(chatId: String): Flow<List<DomainMessage>>
    fun getAccessibleMessagesForUser(userId: String): Flow<List<DomainMessage>>
    suspend fun upsertMessages(newMessages: List<DomainMessage>)
}