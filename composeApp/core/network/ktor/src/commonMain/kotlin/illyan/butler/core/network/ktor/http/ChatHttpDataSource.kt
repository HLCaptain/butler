package illyan.butler.core.network.ktor.http

import illyan.butler.core.local.datasource.UserLocalDataSource
import illyan.butler.core.network.datasource.ChatNetworkDataSource
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.core.network.mapping.toNetworkModel
import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class ChatHttpDataSource(
    private val clientFactory: (Source.Server) -> HttpClient,
    private val userLocalDataSource: UserLocalDataSource
) : ChatNetworkDataSource {
    private var newChatsStateFlow = MutableStateFlow<Set<Chat>?>(null)
    private var isLoadingNewChatsWebSocketSession = false
    private var isLoadedNewChatsWebSocketSession = false

    private fun createNewChatsFlow() = flow {
        while (true) {
            val allChats = fetch()
            Napier.v { "Receiving new chats" }
            emit(allChats)
            delay(5000)
        }
    }

    override fun fetchByChatId(chatId: Uuid): Flow<Chat> {
        return fetchNewChats().map { chats ->
            chats.first { it.id == chatId }
        }
    }

    override fun fetchByUserId(userId: Uuid): Flow<List<Chat>> {
        return fetchNewChats().map { chats ->
            chats.filter { it.ownerId == userId }
        }
    }

    override fun fetchNewChats(): Flow<List<Chat>> {
        return if (newChatsStateFlow.value == null && !isLoadingNewChatsWebSocketSession && !isLoadedNewChatsWebSocketSession) {
            isLoadingNewChatsWebSocketSession = true
            flow {
                isLoadedNewChatsWebSocketSession = true
                isLoadingNewChatsWebSocketSession = false
                Napier.v { "Created new chat flow, emitting chats" }
                emitAll(newChatsStateFlow)
                createNewChatsFlow().collect { newChats -> newChatsStateFlow.update { newChats.toSet() } }
            }
        } else {
            newChatsStateFlow
        }.filterNotNull().map { it.toList() }
    }

    override suspend fun fetchPaginated(source: Source.Server, limit: Int, timestamp: Long): List<Chat> {
        return client.get("/chats") {
            parameter("limit", limit)
            parameter("timestamp", timestamp)
        }.body<List<ChatDto>>().map { it.toDomainModel() }
    }

    override suspend fun fetch(): List<Chat> {
        return client.get("/chats").body<List<ChatDto>>().map { it.toDomainModel() }
    }

    override suspend fun fetchByModel(modelId: String): List<Chat> {
        TODO("Not yet implemented")
    }

    // To avoid needless updates to chats right after they are createdAt
    private val dontUpdateChat = mutableSetOf<Chat>()
    override suspend fun upsert(chat: Chat): Chat {
        return if (chat.id == null) {
            val newChat = client.post("/chats") { setBody(chat.toNetworkModel()) }.body<ChatDto>().toDomainModel()
            dontUpdateChat.add(newChat)
            newChat
        } else if (chat !in dontUpdateChat) {
            client.put("/chats/${chat.id}") { setBody(chat.toNetworkModel()) }.body<ChatDto>().toDomainModel()
        } else {
            dontUpdateChat.removeIf { it.id == chat.id }
            chat
        }.also { newChatsStateFlow.update { chats -> (chats ?: emptySet()) + setOf(it) } }
    }

    override suspend fun delete(id: Uuid): Boolean {
        return client.delete("/chats/$id").status.isSuccess()
    }
}