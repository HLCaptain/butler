package illyan.butler.services.identity.data.cache

import illyan.butler.services.identity.data.model.chat.ChatDto
import kotlinx.coroutines.flow.Flow

interface ChatCache {
    suspend fun getChat(chatId: String): ChatDto?
    suspend fun getChatsByUser(userId: String): List<ChatDto>
    fun getChangedChatsAffectingUser(userId: String): Flow<ChatDto>
    fun getChangesFromChat(chatId: String): Flow<ChatDto>
    suspend fun setChat(chat: ChatDto): ChatDto
    suspend fun deleteChat(chatId: String): Boolean
}
