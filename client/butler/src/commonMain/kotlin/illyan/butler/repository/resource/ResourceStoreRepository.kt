package illyan.butler.repository.resource

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ResourceNetworkDataSource
import illyan.butler.data.store.ResourceMutableStoreBuilder
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainResource
import illyan.butler.manager.HostManager
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest

@Single
class ResourceStoreRepository(
    resourceMutableStoreBuilder: ResourceMutableStoreBuilder,
    private val resourceNetworkDataSource: ResourceNetworkDataSource,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
    private val hostManager: HostManager
) : ResourceRepository {
    @OptIn(ExperimentalStoreApi::class)
    val resourceMutableStore = resourceMutableStoreBuilder.store

    init {
        coroutineScopeIO.launch {
            hostManager.currentHost.collect {
                Napier.d("Host changed, clearing resource state flows")
                resourceStateFlows.clear()
            }
        }
    }

    private val resourceStateFlows = mutableMapOf<String, StateFlow<Pair<DomainResource?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    override fun getResourceFlow(resourceId: String): StateFlow<Pair<DomainResource?, Boolean>> {
        return resourceStateFlows.getOrPut(resourceId) {
            resourceMutableStore.stream<StoreReadResponse<DomainResource>>(
                StoreReadRequest.cached(resourceId, true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.simpleName}")
                val data = it.dataOrNull()
                Napier.d("Resource id: ${data?.id}")
                data to (it is StoreReadResponse.Loading)
            }.stateIn(
                coroutineScopeIO,
                SharingStarted.Eagerly,
                null to true
            )
        }
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun upsert(resource: DomainResource): String {
        val newResource = if (resource.id == null) {
            resourceNetworkDataSource.upsert(resource.toNetworkModel()).toDomainModel()
        } else resource
        resourceMutableStore.write(
            StoreWriteRequest.of(
                key = newResource.id!!,
                value = newResource,
            )
        )
        return newResource.id
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteResource(resourceId: String): Boolean {
        resourceMutableStore.clear(resourceId)
        return true
    }
}