package illyan.butler.backend.data.ktor.service.secure

import illyan.butler.backend.data.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow

interface SecureMessageService : RPCWithJWT {
    suspend fun fetchNewMessages(): Flow<List<MessageDto>>
    suspend fun fetchByChatId(chatId: String): Flow<List<MessageDto>>
    suspend fun upsert(message: MessageDto): MessageDto
    suspend fun delete(messageId: String, chatId: String): Boolean
    suspend fun fetchById(messageId: String): Flow<MessageDto>
    suspend fun fetchAvailableToUser(): Flow<List<MessageDto>>
}