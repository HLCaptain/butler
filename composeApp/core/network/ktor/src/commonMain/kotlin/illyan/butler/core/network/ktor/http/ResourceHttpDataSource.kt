package illyan.butler.core.network.ktor.http

import illyan.butler.core.network.datasource.ResourceNetworkDataSource
import illyan.butler.core.network.ktor.http.di.KtorHttpClientFactory
import illyan.butler.core.network.mapping.toDomainModel
import illyan.butler.domain.model.Resource
import illyan.butler.shared.model.chat.ResourceDto
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class ResourceHttpDataSource(
    private val clientFactory: KtorHttpClientFactory,
) : ResourceNetworkDataSource {
    private val newResourcesStateFlow = MutableStateFlow<List<Resource>?>(null)
    private var isLoadingNewResourcesWebSocketSession = false
    private var isLoadedNewResourcesWebSocketSession = false

    private suspend fun createNewMessagesFlow(source: Source.Server) {
        Napier.v { "Receiving new messages" }
        coroutineScope {
            launch {
                while (true) {
                    val allResources = fetchByUserOnce(source)
                    newResourcesStateFlow.update { allResources }
                    delay(10000)
                }
            }
        }
    }

    override suspend fun delete(resource: Resource): Boolean {
        return clientFactory(resource.source as Source.Server).delete("/resources/${resource.id}").status.isSuccess()
    }

    override fun fetchNewResources(source: Source.Server): Flow<List<Resource>> {
        return if (newResourcesStateFlow.value == null && !isLoadingNewResourcesWebSocketSession && !isLoadedNewResourcesWebSocketSession) {
            isLoadingNewResourcesWebSocketSession = true
            flow {
                createNewMessagesFlow(source)
                isLoadedNewResourcesWebSocketSession = true
                isLoadingNewResourcesWebSocketSession = false
                Napier.v { "Created new resources flow, emitting resources" }
                emitAll(newResourcesStateFlow)
            }
        } else {
            newResourcesStateFlow
        }.filterNotNull()
    }

    override fun fetchResourceById(source: Source.Server, resourceId: Uuid): Flow<Resource> {
        return fetchNewResources(source).map { resources -> resources.first { it.id == resourceId } }
    }

    // To avoid needless updates to resources right after they are createdAt
    private val dontUpdateResource = mutableSetOf<Resource>()

    override suspend fun create(resource: Resource): Resource {
        val source = resource.source as? Source.Server ?: throw IllegalArgumentException("Resource source must be a Server source")
        val newResource = clientFactory(source).post("/resources") { setBody(resource) }.body<ResourceDto>().toDomainModel(source)
        dontUpdateResource.add(newResource)
        return newResource
    }

    override suspend fun upsert(resource: Resource): Resource {
        val source = resource.source as? Source.Server ?: throw IllegalArgumentException("Resource source must be a Server source")
        return if (resource !in dontUpdateResource) {
            clientFactory(source).put("/resources/${resource.id}") { setBody(resource) }.body<ResourceDto>().toDomainModel(source)
        } else {
            dontUpdateResource.removeIf { it.id == resource.id }
            resource
        }
    }

    override fun fetchByUser(source: Source.Server): Flow<List<Resource>> {
        return fetchNewResources(source)
    }

    private suspend fun fetchByUserOnce(source: Source.Server): List<Resource> {
        return clientFactory(source).get("/resources").body<List<ResourceDto>>().map { it.toDomainModel(source) }
    }
}