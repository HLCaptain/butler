package illyan.butler.data.chat

import illyan.butler.domain.model.DomainChat
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getChatFlow(chatId: String, deviceOnly: Boolean): Flow<DomainChat?>
    fun getUserChatsFlow(userId: String, deviceOnly: Boolean): Flow<List<DomainChat>>
    suspend fun upsert(chat: DomainChat, deviceOnly: Boolean): String
    suspend fun deleteAllChats(userId: String, deviceOnly: Boolean)
    suspend fun deleteChat(chatId: String, deviceOnly: Boolean)
}
