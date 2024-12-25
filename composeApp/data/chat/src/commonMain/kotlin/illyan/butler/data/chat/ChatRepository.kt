package illyan.butler.data.chat

import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatFlow(chatId: String, deviceOnly: Boolean): Flow<Pair<DomainChat?, Boolean>>
    fun getUserChatsFlow(userId: String, deviceOnly: Boolean): Flow<Pair<List<DomainChat>?, Boolean>>
    suspend fun upsert(chat: DomainChat, deviceOnly: Boolean): String
    suspend fun deleteAllChats(userId: String)
    suspend fun deleteChat(chatId: String)
}
