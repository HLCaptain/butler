package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.MessageService
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.di.KoinNames
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class MessageRpcDataSource(
    private val messageService: MessageService,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : MessageNetworkDataSource {
    override fun fetchNewMessages(): Flow<List<MessageDto>> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(messageService.fetchNewMessages())
            }
        }
    }

    override fun fetchByChatId(chatId: String): Flow<List<MessageDto>> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(messageService.fetchByChatId(chatId))
            }
        }
    }

    override suspend fun upsert(message: MessageDto): MessageDto {
        return messageService.upsert(message)
    }

    override suspend fun delete(messageId: String, chatId: String): Boolean {
        return messageService.delete(messageId, chatId)
    }

    override fun fetchById(messageId: String): Flow<MessageDto> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(messageService.fetchById(messageId))
            }
        }
    }

    override fun fetchAvailableToUser(): Flow<List<MessageDto>> {
        return flow {
            coroutineScopeIO.launch {
                emitAll(messageService.fetchAvailableToUser())
            }
        }
    }
}