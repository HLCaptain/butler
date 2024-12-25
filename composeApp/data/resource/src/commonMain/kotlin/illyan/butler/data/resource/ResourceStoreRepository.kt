package illyan.butler.data.resource

import illyan.butler.domain.model.DomainResource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class ResourceStoreRepository(
    resourceMutableStoreBuilder: ResourceMutableStoreBuilder
) : ResourceRepository {
    @OptIn(ExperimentalStoreApi::class)
    val resourceMutableStore = resourceMutableStoreBuilder.store

    private val resourceStateFlows = mutableMapOf<String, Flow<DomainResource?>>()

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteAllResources() {
        resourceMutableStore.clear(ResourceKey.Delete.All)
    }

    @OptIn(ExperimentalStoreApi::class)
    override fun getResourceFlow(resourceId: String, deviceOnly: Boolean): Flow<DomainResource?> {
        return resourceStateFlows.getOrPut(resourceId) {
            resourceMutableStore.stream<StoreReadResponse<DomainResource>>(
                StoreReadRequest.cached(ResourceKey.Read.ByResourceId(resourceId), !deviceOnly)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.qualifiedName}")
                val data = it.dataOrNull()
                Napier.d("Resource id: ${data?.id}")
                data
            }
        }
    }

    @OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class)
    override suspend fun upsert(resource: DomainResource, deviceOnly: Boolean): String {
        val newResource = (resourceMutableStore.write(
            StoreWriteRequest.of(
                key = if (deviceOnly) ResourceKey.Write.DeviceOnly else if (resource.id == null) ResourceKey.Write.Create else ResourceKey.Write.Upsert,
                value = resource.copy(id = resource.id ?: Uuid.random().toString()), // ID cannot be null on write
            )
        ) as? StoreWriteResponse.Success.Typed<DomainResource>)?.value
        return newResource?.id!!
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteResource(resourceId: String): Boolean {
        resourceMutableStore.clear(ResourceKey.Delete.ByResourceId(resourceId))
        return true
    }
}
