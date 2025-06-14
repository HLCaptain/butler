package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.ResourceNetworkDataSource
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.domain.model.Resource
import illyan.butler.shared.model.chat.ResourceDto
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
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class ResourceHttpDataSource(private val client: HttpClient) : ResourceNetworkDataSource {
    private val newResourcesStateFlow = MutableStateFlow<List<Resource>?>(null)
    private var isLoadingNewResourcesWebSocketSession = false
    private var isLoadedNewResourcesWebSocketSession = false

    private suspend fun createNewMessagesFlow() {
        Napier.v { "Receiving new messages" }
        coroutineScope {
            launch {
                while (true) {
                    val allResources = fetchByUserOnce()
                    newResourcesStateFlow.update { allResources }
                    delay(10000)
                }
            }
        }
    }

    override suspend fun delete(resourceId: String): Boolean {
        return client.delete("/resources/$resourceId").status.isSuccess()
    }

    override fun fetchNewResources(): Flow<List<Resource>> {
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
    override fun fetchResourceById(resourceId: String): Flow<Resource> {
        return fetchNewResources().map { resources -> resources.first { it.id == resourceId } }
    }

    // To avoid needless updates to resources right after they are createdAt
    private val dontUpdateResource = mutableSetOf<Resource>()
    override suspend fun upsert(resource: Resource): Resource {
        return if (resource.id == null) {
            val newMessage = client.post("/resources") { setBody(resource) }.body<ResourceDto>().toDomainModel()
            dontUpdateResource.add(newMessage)
            newMessage
        } else if (resource !in dontUpdateResource) {
            client.put("/resources/${resource.id}") { setBody(resource) }.body<ResourceDto>().toDomainModel()
        } else {
            dontUpdateResource.removeIf { it.id == resource.id }
            resource
        }
    }

    override fun fetchByUser(): Flow<List<Resource>> {
        return fetchNewResources()
    }

    private suspend fun fetchByUserOnce(): List<Resource> {
        return client.get("/resources").body<List<ResourceDto>>().map { it.toDomainModel() }
    }
}