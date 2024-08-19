package illyan.butler.data.ktor.rpc.service

import illyan.butler.data.network.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.RPC

interface MessageService : RPC {
    suspend fun fetchNewMessages(): Flow<List<MessageDto>>
    suspend fun fetchByChatId(chatId: String): Flow<List<MessageDto>>
    suspend fun upsert(message: MessageDto): MessageDto
    suspend fun delete(messageId: String, chatId: String): Boolean
    suspend fun fetchById(messageId: String): Flow<MessageDto>
    suspend fun fetchAvailableToUser(): Flow<List<MessageDto>>
}