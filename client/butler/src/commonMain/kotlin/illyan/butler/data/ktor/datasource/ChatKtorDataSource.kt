package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.ChatDto
import io.ktor.client.HttpClient
import org.koin.core.annotation.Single

@Single
class ChatKtorDataSource(
    private val client: HttpClient
) : ChatNetworkDataSource {
    override suspend fun fetchChat(uuid: String): ChatDto {
        TODO("Not yet implemented")
    }

    override suspend fun fetchChatsByUser(userUUID: String): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchChatsByModel(modelUUID: String): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun upsertChat(chat: ChatDto): ChatDto {
        TODO("Not yet implemented")
    }

    override suspend fun deleteChat(uuid: String): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteChatsForUser(userUUID: String): Boolean {
        TODO("Not yet implemented")
    }
}