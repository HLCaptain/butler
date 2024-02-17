package illyan.butler.data.ktorfit.datasource

import illyan.butler.data.ktorfit.api.MessageApi
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.MessageDto
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single

@Single
class MessageKtorDataSource(
    private val messageApi: MessageApi
): MessageNetworkDataSource {
    override fun fetch(uuid: String): Flow<MessageDto> = flow {
        emit(messageApi.fetchMessage(uuid))
    }.catch { exception ->
        Napier.e("There was a problem while loading message", exception)
    }

    override fun fetchByChat(chatUUID: String): Flow<List<MessageDto>> = flow {
        emit(messageApi.fetchMessagesByChat(chatUUID))
    }.catch { exception ->
        Napier.e("There was a problem while loading messages", exception)
    }

    override suspend fun upsert(message: MessageDto) {
        messageApi.upsertMessage(message)
    }

    override suspend fun delete(uuid: String) {
        messageApi.deleteMessage(uuid)
    }

    override suspend fun deleteForChat(chatUUID: String) {
        messageApi.deleteMessagesForChat(chatUUID)
    }
}