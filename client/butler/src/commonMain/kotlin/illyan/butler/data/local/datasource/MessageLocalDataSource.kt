package illyan.butler.data.local.datasource

import illyan.butler.domain.model.DomainMessage
import kotlinx.coroutines.flow.Flow

interface MessageLocalDataSource {
    suspend fun insertMessage(message: DomainMessage)
    suspend fun insertMessages(messages: List<DomainMessage>)
    suspend fun updateMessage(message: DomainMessage)
    suspend fun deleteMessage(messageId: String)
    suspend fun deleteAllMessages()
    suspend fun deleteAllMessagesForChat(chatId: String)
    fun getAllMessagesForChat(chatId: String): Flow<List<DomainMessage>>
}