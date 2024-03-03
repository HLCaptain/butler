package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.MessageDto
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single

@Single
class MessageKtorDataSource(
    private val client: HttpClient
) : MessageNetworkDataSource {
    override suspend fun fetchMessage(uuid: String): MessageDto {
        TODO("Not yet implemented")
    }

    override suspend fun fetchMessagesByChat(chatUUID: String): List<MessageDto> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertMessage(message: MessageDto): MessageDto {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessage(uuid: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteMessagesForChat(chatUUID: String): Boolean {
        TODO("Not yet implemented")
    }
}