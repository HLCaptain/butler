package illyan.butler.core.network.datasource

import illyan.butler.domain.model.Resource
import kotlinx.coroutines.flow.Flow

interface ResourceNetworkDataSource {
    fun fetchNewResources(): Flow<List<Resource>>
    fun fetchResourceById(resourceId: String): Flow<Resource>
    suspend fun upsert(resource: Resource): Resource
    fun fetchByUser(): Flow<List<Resource>>
    suspend fun delete(resourceId: String): Boolean
}