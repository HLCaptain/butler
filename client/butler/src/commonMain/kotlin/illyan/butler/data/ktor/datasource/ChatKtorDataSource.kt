package illyan.butler.data.ktor.datasource

import illyan.butler.data.ktor.utils.WebSocketSessionManager
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
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
class ChatKtorDataSource(
    private val client: HttpClient,
    private val webSocketSessionManager: WebSocketSessionManager,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope
) : ChatNetworkDataSource {
    private var newChatsStateFlow = MutableStateFlow<List<ChatDto>?>(null)
    private var isLoadingNewChatsWebSocketSession = false
    private var isLoadedNewChatsWebSocketSession = false

    private suspend fun createNewChatsFlow() {
        Napier.v { "Receiving new chats" }
//        val session = webSocketSessionManager.createSession("/chats")
//        coroutineScopeIO.launch {
//            session.incoming.receiveAsFlow().collect { _ ->
//                Napier.v { "Received new chat" }
//                newChatsStateFlow.update { session.receiveDeserialized() }
//            }
//        }
        // TODO: remove when websockets are fixed
        coroutineScopeIO.launch {
            while (true) {
                val allChats = fetch()
                newChatsStateFlow.update { allChats }
                delay(5000)
            }
        }
    }

    override fun fetchNewChats(): Flow<List<ChatDto>> {
        return if (newChatsStateFlow.value == null && !isLoadingNewChatsWebSocketSession && !isLoadedNewChatsWebSocketSession) {
            isLoadingNewChatsWebSocketSession = true
            flow {
                createNewChatsFlow()
                isLoadedNewChatsWebSocketSession = true
                isLoadingNewChatsWebSocketSession = false
                Napier.v { "Created new chat flow, emitting chats" }
                emitAll(newChatsStateFlow)
            }
        } else {
            newChatsStateFlow
        }.filterNotNull()
    }

    override suspend fun fetchPaginated(limit: Int, timestamp: Long): List<ChatDto> {
        return client.get("/chats") {
            parameter("limit", limit)
            parameter("timestamp", timestamp)
        }.body()
    }

    override suspend fun fetch(): List<ChatDto> {
        return client.get("/chats").body()
    }

    override suspend fun fetchByModel(modelUUID: String): List<ChatDto> {
        TODO("Not yet implemented")
    }

    // To avoid needless updates to chats right after they are created
    private val dontUpdateChat = mutableSetOf<ChatDto>()
    override suspend fun upsert(chat: ChatDto): ChatDto {
        return if (chat.id == null) {
            val newChat = client.post("/chats") { setBody(chat) }.body<ChatDto>()
            dontUpdateChat.add(newChat)
            newChat
        } else if (chat !in dontUpdateChat) {
            client.put("/chats/${chat.id}") { setBody(chat) }.body()
        } else {
            dontUpdateChat.removeIf { it.id == chat.id }
            chat
        }. also { newChatsStateFlow.update { _ -> listOf(it) } }
    }

    override suspend fun delete(uuid: String): Boolean {
        return client.delete("/chats/$uuid").status.isSuccess()
    }
}