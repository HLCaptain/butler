package illyan.butler.data.ktor.datasource

import illyan.butler.data.ktor.utils.WebSocketSessionManager
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.di.KoinNames
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class MessageKtorDataSource(
    private val client: HttpClient,
    private val webSocketSessionManager: WebSocketSessionManager,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : MessageNetworkDataSource {
    private val newMessagesStateFlow = MutableStateFlow<List<MessageDto>?>(null)
    private var isLoadingNewMessagesWebSocketSession = false
    private var isLoadedMeWebSocketSession = false

    private suspend fun createNewMessagesFlow() {
        Napier.v { "Receiving new messages" }
        val session = webSocketSessionManager.createSession("/messages")
        coroutineScopeIO.launch {
            session.incoming.receiveAsFlow().collect { _ ->
                Napier.v { "Received new messages" }
                newMessagesStateFlow.update { session.receiveDeserialized() }
            }
        }
    }

    override fun fetchNewMessages(): Flow<List<MessageDto>> {
        return if (newMessagesStateFlow.value == null && !isLoadingNewMessagesWebSocketSession && !isLoadedMeWebSocketSession) {
            isLoadingNewMessagesWebSocketSession = true
            flow {
                createNewMessagesFlow()
                isLoadedMeWebSocketSession = true
                isLoadingNewMessagesWebSocketSession = false
                emitAll(newMessagesStateFlow)
            }
        } else {
            newMessagesStateFlow
        }.map { it ?: emptyList() }
    }

    override suspend fun fetchByChat(chatUUID: String, limit: Int, timestamp: Long): List<MessageDto> {
        return client.get("/chats/$chatUUID/messages") {
            parameter("limit", limit)
            parameter("timestamp", timestamp)
        }.body()
    }

    override suspend fun fetchByChat(chatUUID: String): List<MessageDto> {
        return client.get("/chats/$chatUUID/messages").body()
    }

    override suspend fun upsert(message: MessageDto): MessageDto {
        return if (message.id == null) {
            client.post("/chats/${message.chatId}/messages") { setBody(message) }
        } else {
            client.put("/chats/${message.chatId}/messages/${message.id}") { setBody(message) }
        }.body()
    }

    override suspend fun delete(id: String, chatId: String): Boolean {
        return client.delete("/chats/$chatId/messages/$id").status.isSuccess()
    }

    override suspend fun fetch(key: String): MessageDto? {
        return client.get("/messages/$key").body()
    }
}