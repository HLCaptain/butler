package illyan.butler.data.ktor.datasource

import illyan.butler.data.ktor.utils.WebSocketSessionManager
import illyan.butler.data.network.datasource.ResourceNetworkDataSource
import illyan.butler.data.network.model.chat.ResourceDto
import illyan.butler.di.KoinNames
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.receiveDeserialized
import io.ktor.client.request.delete
import io.ktor.client.request.get
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
) : ResourceNetworkDataSource {
    private val newResourcesStateFlow = MutableStateFlow<List<ResourceDto>?>(null)
    private var isLoadingNewResourcesWebSocketSession = false
    private var isLoadedNewResourcesWebSocketSession = false

    private suspend fun createNewMessagesFlow() {
        Napier.v { "Receiving new messages" }
        val session = webSocketSessionManager.createSession("/resources")
        coroutineScopeIO.launch {
            session.incoming.receiveAsFlow().collect { _ ->
                val messages = session.receiveDeserialized<List<ResourceDto>?>()
                Napier.v { "Received new ${messages?.size} messages " }
                newResourcesStateFlow.update { messages }
            }
        }
        // TODO: remove when websockets are fixed
        coroutineScopeIO.launch {
            while (true) {
                val allMessages = fetchByUser()
                newResourcesStateFlow.update { allMessages }
                delay(10000)
            }
        }
    }

    override suspend fun delete(resourceId: String): Boolean {
        return client.delete("/resources/$resourceId").status.isSuccess()
    }

    override fun fetchNewResources(): Flow<List<ResourceDto>> {
        return if (newResourcesStateFlow.value == null && !isLoadingNewResourcesWebSocketSession && !isLoadedNewResourcesWebSocketSession) {
            isLoadingNewResourcesWebSocketSession = true
            flow {
                createNewMessagesFlow()
                isLoadedNewResourcesWebSocketSession = true
                isLoadingNewResourcesWebSocketSession = false
                Napier.v { "Created new resources flow, emitting resources" }
                emitAll(newResourcesStateFlow)
            }
        } else {
            newResourcesStateFlow
        }.filterNotNull()
    }
    override suspend fun fetchResource(resourceId: String): ResourceDto? {
        return client.get("/resources/$resourceId").body<ResourceDto?>()
    }

    // To avoid needless updates to resources right after they are created
    private val dontUpdateResource = mutableSetOf<ResourceDto>()
    override suspend fun upsert(resource: ResourceDto): ResourceDto {
        return if (resource.id == null) {
            val newMessage = client.post("/resources") { setBody(resource) }.body<ResourceDto>()
            dontUpdateResource.add(newMessage)
            newMessage
        } else if (resource !in dontUpdateResource) {
            client.put("/resources/${resource.id}") { setBody(resource) }.body()
        } else {
            dontUpdateResource.removeIf { it.id == resource.id }
            resource
        }
    }

    override suspend fun fetchByUser(): List<ResourceDto> {
        return client.get("/resources").body()
    }
}