package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.ChatService
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.core.annotation.Single

@Single
class ChatRpcDataSource(
    private val chatService: StateFlow<ChatService?>,
) : ChatNetworkDataSource {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchNewChats(): Flow<List<ChatDto>> {
        return chatService.flatMapLatest { service ->
            service?.fetchNewChats() ?: emptyFlow()
        }
    }

    override suspend fun fetchPaginated(limit: Int, timestamp: Long): List<ChatDto> {
        return chatService.value?.fetchPaginated(limit, timestamp) ?: throw IllegalStateException("ChatService is not available")
    }

    override suspend fun fetch(): List<ChatDto> {
        return chatService.value?.fetch() ?: throw IllegalStateException("ChatService is not available")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchByChatId(chatId: String): Flow<ChatDto> {
        return chatService.flatMapLatest { service ->
            service?.fetchByChatId(chatId) ?: emptyFlow()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchByUserId(userId: String): Flow<List<ChatDto>> {
        return chatService.flatMapLatest { service ->
            service?.fetchByUserId(userId) ?: emptyFlow()
        }
    }

    override suspend fun fetchByModel(modelId: String): List<ChatDto> {
        return chatService.value?.fetchByModel(modelId) ?: throw IllegalStateException("ChatService is not available")
    }

    override suspend fun upsert(chat: ChatDto): ChatDto {
        return chatService.value?.upsert(chat) ?: throw IllegalStateException("ChatService is not available")
    }

    override suspend fun delete(id: String): Boolean {
        return chatService.value?.delete(id) ?: throw IllegalStateException("ChatService is not available")
    }
}