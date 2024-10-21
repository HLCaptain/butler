package illyan.butler.backend.data.datasource

import illyan.butler.backend.data.model.chat.ResourceDto

interface ResourceDataSource {
    suspend fun createResource(userId: String, resource: ResourceDto): ResourceDto
    suspend fun getResource(userId: String, resourceId: String): ResourceDto?
    suspend fun deleteResource(userId: String, resourceId: String): Boolean
    suspend fun getResources(userId: String): List<ResourceDto>
}
