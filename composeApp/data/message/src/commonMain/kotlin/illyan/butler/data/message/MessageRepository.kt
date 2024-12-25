package illyan.butler.data.message

import illyan.butler.domain.model.DomainMessage
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessageFlow(messageId: String, deviceOnly: Boolean): Flow<Pair<DomainMessage?, Boolean>>
    fun getChatMessagesFlow(chatId: String, deviceOnly: Boolean): Flow<Pair<List<DomainMessage>?, Boolean>>
    fun getUserMessagesFlow(userId: String, deviceOnly: Boolean): Flow<Pair<List<DomainMessage>?, Boolean>>
    suspend fun upsert(message: DomainMessage, deviceOnly: Boolean): String
}
