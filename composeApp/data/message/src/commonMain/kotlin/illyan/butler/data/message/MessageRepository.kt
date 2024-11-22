package illyan.butler.data.message

import illyan.butler.domain.model.DomainMessage
import kotlinx.coroutines.flow.Flow

interface MessageRepository {
    fun getMessageFlow(messageId: String): Flow<Pair<DomainMessage?, Boolean>>
    fun getChatMessagesFlow(chatId: String): Flow<Pair<List<DomainMessage>?, Boolean>>
    suspend fun upsert(message: DomainMessage): String
    fun getUserMessagesFlow(userId: String): Flow<Pair<List<DomainMessage>?, Boolean>>
}
