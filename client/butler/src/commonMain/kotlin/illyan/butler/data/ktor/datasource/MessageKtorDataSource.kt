package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.MessageDto
import io.ktor.client.*
import org.koin.core.annotation.Single

@Single
class MessageKtorDataSource(
    private val client: HttpClient
) : MessageNetworkDataSource {
    override suspend fun fetch(uuid: String): MessageDto {
        TODO("Not yet implemented")
    }

    override suspend fun fetchByChat(chatUUID: String): List<MessageDto> {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(message: MessageDto): MessageDto {
        TODO("Not yet implemented")
    }

    override suspend fun delete(uuid: String): Boolean {
        TODO("Not yet implemented")
    }
}