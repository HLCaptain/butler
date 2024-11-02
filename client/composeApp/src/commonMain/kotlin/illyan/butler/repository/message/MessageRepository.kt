package illyan.butler.repository.message

import illyan.butler.model.DomainMessage
import kotlinx.coroutines.flow.StateFlow

interface MessageRepository {
    fun getMessageFlow(messageId: String): StateFlow<Pair<DomainMessage?, Boolean>>
    fun getChatMessagesFlow(chatId: String): StateFlow<Pair<List<DomainMessage>?, Boolean>>
    suspend fun upsert(message: DomainMessage): String
    fun getUserMessagesFlow(userId: String): StateFlow<Pair<List<DomainMessage>?, Boolean>>
}
