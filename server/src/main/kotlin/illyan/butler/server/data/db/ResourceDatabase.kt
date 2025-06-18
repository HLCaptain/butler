package illyan.butler.server.data.db

import illyan.butler.shared.model.chat.ResourceDto
import kotlinx.coroutines.flow.Flow

interface ResourceDatabase {
    suspend fun createResource(userId: String, resource: ResourceDto): ResourceDto
    suspend fun getResource(userId: String, resourceId: String): ResourceDto
    fun getResourceFlow(userId: String, resourceId: String): Flow<ResourceDto>
    suspend fun deleteResource(userId: String, resourceId: String): Boolean
    suspend fun getResources(userId: String): List<ResourceDto>
    fun getResourcesFlow(userId: String): Flow<List<ResourceDto>>
}
