package illyan.butler.core.network.datasource

import illyan.butler.domain.model.Resource
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ResourceNetworkDataSource {
    fun fetchNewResources(source: Source.Server): Flow<List<Resource>>
    fun fetchResourceById(source: Source.Server, resourceId: Uuid): Flow<Resource>
    suspend fun create(resource: Resource): Resource
    suspend fun upsert(resource: Resource): Resource
    fun fetchByUser(source: Source.Server): Flow<List<Resource>>
    suspend fun delete(resource: Resource): Boolean
}