package illyan.butler.data.chat

import illyan.butler.domain.model.DomainChat
import illyan.butler.model.DomainChat
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    fun getChatFlow(chatId: String): StateFlow<Pair<DomainChat?, Boolean>>
    fun getUserChatsFlow(userId: String): StateFlow<Pair<List<DomainChat>?, Boolean>>
    suspend fun upsert(chat: DomainChat): String
    suspend fun deleteAllChats(userId: String)
    suspend fun deleteChat(chatId: String)
}