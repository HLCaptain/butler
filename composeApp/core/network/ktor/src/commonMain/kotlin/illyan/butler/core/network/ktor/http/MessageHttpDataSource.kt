package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.MessageNetworkDataSource
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.domain.model.DomainMessage
import illyan.butler.shared.model.chat.MessageDto
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

@Single
class MessageHttpDataSource(private val client: HttpClient) : MessageNetworkDataSource {
    private val newMessagesStateFlow = MutableStateFlow<List<DomainMessage>?>(null)
    private var isLoadingNewMessagesWebSocketSession = false
    private var isLoadedNewMessagesWebSocketSession = false

    private suspend fun createNewMessagesFlow() {
        Napier.v { "Receiving new messages" }
        coroutineScope {
            launch {
                while (true) {
                    val allMessages = fetchByUser()
                    newMessagesStateFlow.update { allMessages }
                    delay(10000)
                }
            }
        }
    }

    override fun fetchNewMessages(): Flow<List<DomainMessage>> {
        return if (newMessagesStateFlow.value == null && !isLoadingNewMessagesWebSocketSession && !isLoadedNewMessagesWebSocketSession) {
            isLoadingNewMessagesWebSocketSession = true
            flow {
                createNewMessagesFlow()
                isLoadedNewMessagesWebSocketSession = true
                isLoadingNewMessagesWebSocketSession = false
                Napier.v { "Created new message flow, emitting messages" }
                emitAll(newMessagesStateFlow)
            }
        } else {
            newMessagesStateFlow
        }.filterNotNull()
    }

    override fun fetchByChatId(chatId: String): Flow<List<DomainMessage>> {
        return fetchNewMessages().filter { messages -> messages.any { it.chatId == chatId } }
    }

    // To avoid needless updates to messages right after they are created
    private val dontUpdateMessage = mutableSetOf<DomainMessage>()
    override suspend fun upsert(message: DomainMessage): DomainMessage {
        return if (message.id == null) {
            val newMessage = client.post("/chats/${message.chatId}/messages") { setBody(message) }.body<MessageDto>().toDomainModel()
            dontUpdateMessage.add(newMessage)
            newMessage
        } else if (message !in dontUpdateMessage) {
            client.put("/chats/${message.chatId}/messages/${message.id}") { setBody(message) }.body<MessageDto>().toDomainModel()
        } else {
            dontUpdateMessage.removeIf { it.id == message.id }
            message
        }.also { newMessagesStateFlow.update { _ -> listOf(it) } }
    }

    override suspend fun delete(messageId: String, chatId: String): Boolean {
        return client.delete("/chats/$chatId/messages/$messageId").status.isSuccess()
    }

    override fun fetchById(messageId: String): Flow<DomainMessage> {
        return fetchNewMessages().map { messages -> messages.first { it.id == messageId } }
    }

    override fun fetchAvailableToUser(): Flow<List<DomainMessage>> {
        return fetchNewMessages()
    }

    private suspend fun fetchByUser(): List<DomainMessage> {
        return client.get("/messages").body()
    }
}
