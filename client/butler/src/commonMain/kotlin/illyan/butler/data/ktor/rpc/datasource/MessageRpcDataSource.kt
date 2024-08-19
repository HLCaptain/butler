package illyan.butler.data.ktor.rpc.datasource

import illyan.butler.data.ktor.rpc.service.MessageService
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.chat.MessageDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import org.koin.core.annotation.Single

@Single
class MessageRpcDataSource(
    private val messageService: StateFlow<MessageService?>,
) : MessageNetworkDataSource {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchNewMessages(): Flow<List<MessageDto>> {
        return messageService.flatMapLatest { service ->
            service?.fetchNewMessages() ?: emptyFlow()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchByChatId(chatId: String): Flow<List<MessageDto>> {
        return messageService.flatMapLatest { service ->
            service?.fetchByChatId(chatId) ?: emptyFlow()
        }
    }

    override suspend fun upsert(message: MessageDto): MessageDto {
        return messageService.value?.upsert(message) ?: throw IllegalStateException("MessageService is not available")
    }

    override suspend fun delete(messageId: String, chatId: String): Boolean {
        return messageService.value?.delete(messageId, chatId) ?: throw IllegalStateException("MessageService is not available")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchById(messageId: String): Flow<MessageDto> {
        return messageService.flatMapLatest { service ->
            service?.fetchById(messageId) ?: emptyFlow()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun fetchAvailableToUser(): Flow<List<MessageDto>> {
        return messageService.flatMapLatest { service ->
            service?.fetchAvailableToUser() ?: emptyFlow()
        }
    }
}