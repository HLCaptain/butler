package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.ChatService
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
import illyan.butler.di.KoinNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class ChatRpcDataSource(
    private val chatService: ChatService,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : ChatNetworkDataSource {
    override fun fetchNewChats(): Flow<List<ChatDto>> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(chatService.fetchNewChats())
            }
        }
    }

    override suspend fun fetchPaginated(limit: Int, timestamp: Long): List<ChatDto> {
        return chatService.fetchPaginated(limit, timestamp)
    }

    override suspend fun fetch(): List<ChatDto> {
        return chatService.fetch()
    }

    override fun fetchByChatId(chatId: String): Flow<ChatDto> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(chatService.fetchByChatId(chatId))
            }
        }
    }

    override fun fetchByUserId(userId: String): Flow<List<ChatDto>> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(chatService.fetchByUserId(userId))
            }
        }
    }

    override suspend fun fetchByModel(modelId: String): List<ChatDto> {
        return chatService.fetchByModel(modelId)
    }

    override suspend fun upsert(chat: ChatDto): ChatDto {
        return chatService.upsert(chat)
    }

    override suspend fun delete(id: String): Boolean {
        return chatService.delete(id)
    }
}