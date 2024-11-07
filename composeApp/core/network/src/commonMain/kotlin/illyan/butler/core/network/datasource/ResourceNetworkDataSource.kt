package illyan.butler.core.network.datasource

import illyan.butler.shared.model.chat.ResourceDto
import kotlinx.coroutines.flow.Flow

interface ResourceNetworkDataSource {
    fun fetchNewResources(): Flow<List<ResourceDto>>
    fun fetchResourceById(resourceId: String): Flow<ResourceDto>
    suspend fun upsert(resource: ResourceDto): ResourceDto
    fun fetchByUser(): Flow<List<ResourceDto>>
    suspend fun delete(resourceId: String): Boolean
}