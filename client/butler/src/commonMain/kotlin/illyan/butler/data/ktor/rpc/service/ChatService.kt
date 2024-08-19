package illyan.butler.data.ktor.rpc.service

import illyan.butler.data.network.model.chat.ChatDto
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.RPC

interface ChatService : RPC {
    suspend fun fetchNewChats(): Flow<List<ChatDto>>
    suspend fun fetchPaginated(limit: Int, timestamp: Long): List<ChatDto>
    suspend fun fetch(): List<ChatDto>
    suspend fun fetchByChatId(chatId: String): Flow<ChatDto>
    suspend fun fetchByUserId(userId: String): Flow<List<ChatDto>>
    suspend fun fetchByModel(modelId: String): List<ChatDto>
    suspend fun upsert(chat: ChatDto): ChatDto
    suspend fun delete(id: String): Boolean
}