package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.MessageNetworkDataSource
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.MessageDto
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class MessageHttpDataSource(
    private val clientFactory: (Source.Server) -> HttpClient,
) : MessageNetworkDataSource {
    private val newMessagesStateFlow = MutableStateFlow<List<Message>?>(null)
    private var isLoadingNewMessagesWebSocketSession = false
    private var isLoadedNewMessagesWebSocketSession = false

    private suspend fun createNewMessagesFlow(source: Source.Server) {
        Napier.v { "Receiving new messages" }
        coroutineScope {
            launch {
                while (true) {
                    val allMessages = fetchByUser(source)
                    newMessagesStateFlow.update { allMessages }
                    delay(10000)
                }
            }
        }
    }

    override fun fetchNewMessages(source: Source.Server): Flow<List<Message>> {
        return if (newMessagesStateFlow.value == null && !isLoadingNewMessagesWebSocketSession && !isLoadedNewMessagesWebSocketSession) {
            isLoadingNewMessagesWebSocketSession = true
            flow {
                createNewMessagesFlow(source)
                isLoadedNewMessagesWebSocketSession = true
                isLoadingNewMessagesWebSocketSession = false
                Napier.v { "Created new message flow, emitting messages" }
                emitAll(newMessagesStateFlow)
            }
        } else {
            newMessagesStateFlow
        }.filterNotNull()
    }

    override fun fetchByChatId(chatId: Uuid): Flow<List<Message>> {
        return fetchNewMessages().filter { messages -> messages.any { it.chatId == chatId } }
    }

    // To avoid needless updates to messages right after they are createdAt
    private val dontUpdateMessage = mutableSetOf<Message>()
    override suspend fun upsert(message: Message): Message {
        val serverSource = (message.source as? Source.Server) ?: throw IllegalArgumentException("Message source must be a server source")
        val endpoint = serverSource.endpoint
        return if (message.id == null) {
            val newMessage = clientFactory(serverSource).post("/chats/${message.chatId}/messages") { setBody(message) }.body<MessageDto>().toDomainModel(endpoint)
            dontUpdateMessage.add(newMessage)
            newMessage
        } else if (message !in dontUpdateMessage) {
            clientFactory(serverSource).put("/chats/${message.chatId}/messages/${message.id}") { setBody(message) }.body<MessageDto>().toDomainModel(endpoint)
        } else {
            dontUpdateMessage.removeIf { it.id == message.id }
            message
        }.also { newMessagesStateFlow.update { _ -> listOf(it) } }
    }

    override suspend fun delete(message: Message): Boolean {
        return clientFactory(message.source as Source.Server).delete("/chats/${message.chatId}/messages/${message.id}").status.isSuccess()
    }

    override fun fetchById(messageId: Uuid): Flow<Message> {
        return fetchNewMessages().map { messages -> messages.first { it.id == messageId } }
    }

    override fun fetchAvailableToUser(): Flow<List<Message>> {
        return fetchNewMessages()
    }

    private suspend fun fetchByUser(source: Source.Server): List<Message> {
        return clientFactory(source).get("/messages").body()
    }
}
