package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.chat.MessageDto
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class MessageKtorDataSource(
    private val client: HttpClient
) : MessageNetworkDataSource {
    override fun fetch(uuid: String): Flow<MessageDto> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchByChat(chatUUID: String, limit: Int, timestamp: Long): List<MessageDto> {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(message: MessageDto): MessageDto {
        TODO("Not yet implemented")
    }

    override suspend fun delete(uuid: String): Boolean {
        TODO("Not yet implemented")
    }
}