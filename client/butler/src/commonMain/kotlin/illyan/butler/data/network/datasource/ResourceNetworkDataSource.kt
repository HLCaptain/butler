package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.chat.ResourceDto
import kotlinx.coroutines.flow.Flow

interface ResourceNetworkDataSource {
    fun fetchNewResources(): Flow<List<ResourceDto>>
    suspend fun fetchResource(resourceId: String): ResourceDto?
    suspend fun upsert(resource: ResourceDto): ResourceDto
    suspend fun fetchByUser(): List<ResourceDto>
    suspend fun delete(resourceId: String): Boolean
}