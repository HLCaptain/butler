package illyan.butler.data.resource

import illyan.butler.domain.model.Resource
import illyan.butler.shared.model.chat.Source
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

@OptIn(ExperimentalUuidApi::class)
@Single
class ResourceStoreRepository(
    resourceMutableStoreBuilder: ResourceMutableStoreBuilder
) : ResourceRepository {
    @OptIn(ExperimentalStoreApi::class)
    val resourceMutableStore = resourceMutableStoreBuilder.store

    private val resourceStateFlows = mutableMapOf<String, Flow<Resource?>>()

    @OptIn(ExperimentalStoreApi::class)
    override fun getResourceFlow(resourceId: Uuid, source: Source): Flow<Resource?> {
        return resourceStateFlows.getOrPut(resourceId) {
            resourceMutableStore.stream<StoreReadResponse<Resource>>(
                StoreReadRequest.cached(ResourceKey.Read.ByResourceId(resourceId), source is Source.Server)
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
    override suspend fun upsert(resource: Resource): Uuid {
        val newResource = (resourceMutableStore.write(
            StoreWriteRequest.of(
                key = ResourceKey.Write.Upsert(resource),
                value = resource,
            )
        ) as? StoreWriteResponse.Success.Typed<Resource>)?.value
        return newResource?.id
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteResource(resource: Resource): Boolean {
        resourceMutableStore.clear(ResourceKey.Delete.ByResourceId(resourceId, deviceOnly))
        return true
    }
}
