package illyan.butler.data.chat

import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatFlow(chatId: String): Flow<Pair<DomainChat?, Boolean>>
    fun getUserChatsFlow(userId: String): Flow<Pair<List<DomainChat>?, Boolean>>
    suspend fun upsert(chat: DomainChat): String
    suspend fun deleteAllChats(userId: String)
    suspend fun deleteChat(chatId: String)
}