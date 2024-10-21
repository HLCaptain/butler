package illyan.butler.backend.data.ktor.service.secure

import illyan.butler.backend.data.model.chat.ChatDto
import kotlinx.coroutines.flow.Flow

interface SecureChatService : RPCWithJWT {
    suspend fun fetchNewChats(): Flow<List<ChatDto>>
    suspend fun fetch(): List<ChatDto>
    suspend fun fetchByChatId(chatId: String): Flow<ChatDto>
    suspend fun fetchByUserId(): Flow<List<ChatDto>>
    suspend fun fetchByModel(modelId: String): List<ChatDto>
    suspend fun upsert(chat: ChatDto): ChatDto
    suspend fun delete(chatId: String): Boolean
}