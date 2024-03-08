package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.ChatDto
import io.ktor.client.*
import org.koin.core.annotation.Single

@Single
class ChatKtorDataSource(
    private val client: HttpClient
) : ChatNetworkDataSource {
    override suspend fun fetch(uuid: String): ChatDto {
        TODO("Not yet implemented")
    }

    override suspend fun fetchByUser(userUUID: String): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchByModel(modelUUID: String): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(chat: ChatDto): ChatDto {
        TODO("Not yet implemented")
    }

    override suspend fun delete(uuid: String): Boolean {
        TODO("Not yet implemented")
    }
}