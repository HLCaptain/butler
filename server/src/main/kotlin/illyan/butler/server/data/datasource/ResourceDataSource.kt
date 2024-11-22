package illyan.butler.server.data.datasource

import illyan.butler.shared.model.chat.ResourceDto

interface ResourceDataSource {
    suspend fun createResource(userId: String, resource: ResourceDto): ResourceDto
    suspend fun getResource(userId: String, resourceId: String): ResourceDto?
    suspend fun deleteResource(userId: String, resourceId: String): Boolean
    suspend fun getResources(userId: String): List<ResourceDto>
}
