package illyan.butler.data.message

import illyan.butler.domain.model.DomainMessage
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessageFlow(messageId: String, deviceOnly: Boolean): Flow<DomainMessage?>
    fun getChatMessagesFlow(chatId: String, deviceOnly: Boolean): Flow<List<DomainMessage>>
    fun getUserMessagesFlow(userId: String, deviceOnly: Boolean): Flow<List<DomainMessage>>
    suspend fun upsert(message: DomainMessage, deviceOnly: Boolean): String
}
