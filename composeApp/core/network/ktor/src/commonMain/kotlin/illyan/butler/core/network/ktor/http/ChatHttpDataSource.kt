package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.ChatNetworkDataSource
import illyan.butler.core.network.ktor.http.di.KtorHttpClientFactory
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.core.network.mapping.toNetworkModel
import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
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
    private val clientFactory: KtorHttpClientFactory,
) : ChatNetworkDataSource {
    private var newChatsStateFlow = MutableStateFlow<Set<Chat>?>(null)
    private var isLoadingNewChatsWebSocketSession = false
    private var isLoadedNewChatsWebSocketSession = false

    private fun createNewChatsFlow(source: Source.Server) = flow {
        while (true) {
            val allChats = fetch(source)
            Napier.v { "Receiving new chats" }
            emit(allChats)
            delay(5000)
        }
    }

    override fun fetchByChatId(source: Source.Server, chatId: Uuid): Flow<Chat> {
        return fetchNewChats(source).map { chats ->
            chats.first { it.id == chatId }
        }
    }

    override fun fetchByUserId(source: Source.Server): Flow<List<Chat>> {
        return fetchNewChats(source).map { chats ->
            chats.filter { it.source == source }
        }
    }

    override fun fetchNewChats(source: Source.Server): Flow<List<Chat>> {
        return if (newChatsStateFlow.value == null && !isLoadingNewChatsWebSocketSession && !isLoadedNewChatsWebSocketSession) {
            isLoadingNewChatsWebSocketSession = true
            flow {
                isLoadedNewChatsWebSocketSession = true
                isLoadingNewChatsWebSocketSession = false
                Napier.v { "Created new chat flow, emitting chats" }
                emitAll(newChatsStateFlow)
                createNewChatsFlow(source).collect { newChats -> newChatsStateFlow.update { newChats.toSet() } }
            }
        } else {
            newChatsStateFlow
        }.filterNotNull().map { it.toList() }
    }

    override suspend fun fetch(source: Source.Server): List<Chat> {
        return clientFactory(source).get("/chats").body<List<ChatDto>>().map { it.toDomainModel(source) }
    }

    // To avoid needless updates to chats right after they are createdAt
    private val dontUpdateChat = mutableSetOf<Chat>()

    override suspend fun create(chat: Chat): Chat {
        val serverSource = (chat.source as? Source.Server) ?: throw IllegalArgumentException("Chat source must be a server source")
        val newChat = clientFactory(serverSource).post("/chats") { setBody(chat.toNetworkModel()) }.body<ChatDto>().toDomainModel(serverSource)
        dontUpdateChat.add(newChat)
        return newChat.also { newChatsStateFlow.update { chats -> (chats ?: emptySet()) + setOf(it) } }
    }

    override suspend fun upsert(chat: Chat): Chat {
        val serverSource = (chat.source as? Source.Server) ?: throw IllegalArgumentException("Chat source must be a server source")
        return if (chat !in dontUpdateChat) {
            clientFactory(serverSource).put("/chats/${chat.id}") { setBody(chat.toNetworkModel()) }.body<ChatDto>().toDomainModel(serverSource)
        } else {
            dontUpdateChat.removeIf { it.id == chat.id }
            chat
        }.also { newChatsStateFlow.update { chats -> (chats ?: emptySet()) + setOf(it) } }
    }

    override suspend fun delete(chat: Chat): Boolean {
        return clientFactory(chat.source as Source.Server).delete("/chats/${chat.id}").status.isSuccess()
    }
}