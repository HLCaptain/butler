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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Single
class ResourceKtorDataSource(
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
                val messages = session.receiveDeserialized<List<MessageDto>?>()
                Napier.v { "Received new ${messages?.size} messages " }
                newMessagesStateFlow.update { messages }
            }
        }
        // TODO: remove when websockets are fixed
        coroutineScopeIO.launch {
            while (true) {
                val allMessages = fetchByUser()
                newMessagesStateFlow.update { allMessages }
                delay(10000)
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
                Napier.v { "Created new message flow, emitting messages" }
                emitAll(newMessagesStateFlow)
            }
        } else {
            newMessagesStateFlow
        }.filterNotNull()
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

    // To avoid needless updates to messages right after they are created
    private val dontUpdateMessage = mutableSetOf<MessageDto>()
    override suspend fun upsert(message: MessageDto): MessageDto {
        return if (message.id == null) {
            val newMessage = client.post("/chats/${message.chatId}/messages") { setBody(message) }.body<MessageDto>()
            dontUpdateMessage.add(newMessage)
            newMessage
        } else if (message !in dontUpdateMessage) {
            client.put("/chats/${message.chatId}/messages/${message.id}") { setBody(message) }.body()
        } else {
            dontUpdateMessage.removeIf { it.id == message.id }
            message
        }.also { newMessagesStateFlow.update { _ -> listOf(it) } }
    }

    override suspend fun delete(id: String, chatId: String): Boolean {
        return client.delete("/chats/$chatId/messages/$id").status.isSuccess()
    }

    override suspend fun fetch(key: String): MessageDto? {
        return client.get("/messages/$key").body()
    }

    override suspend fun fetchByUser(): List<MessageDto> {
        return client.get("/messages").body()
    }
}