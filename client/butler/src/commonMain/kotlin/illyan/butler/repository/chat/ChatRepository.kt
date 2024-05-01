package illyan.butler.repository.chat

import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    fun getChatFlow(chatId: String): StateFlow<Pair<DomainChat?, Boolean>>
    fun getUserChatsFlow(userId: String): StateFlow<Pair<List<DomainChat>?, Boolean>>
    suspend fun upsert(chat: DomainChat): String
}