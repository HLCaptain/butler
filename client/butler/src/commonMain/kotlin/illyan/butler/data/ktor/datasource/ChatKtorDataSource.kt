package illyan.butler.data.ktor.datasource

import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import org.koin.core.annotation.Single

@Single
class ChatKtorDataSource(
    private val client: HttpClient
) : ChatNetworkDataSource {
    override fun fetch(uuid: String): Flow<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun fetchByUser(userUUID: String, limit: Int, timestamp: Long): List<ChatDto> {
        return client.get("/chats") {
            parameter("limit", limit)
            parameter("timestamp", timestamp)
        }.body()
    }

    override suspend fun fetchByModel(modelUUID: String): List<ChatDto> {
        TODO("Not yet implemented")
    }

    override suspend fun upsert(chat: ChatDto): ChatDto {
        return if (chat.id == null) {
            client.post("/chats") { setBody(chat) }
        } else {
            client.put("/chats/${chat.id}") { setBody(chat) }
        }.body()
    }

    override suspend fun delete(uuid: String): Boolean {
        return client.delete("/chats/$uuid").status.isSuccess()
    }
}