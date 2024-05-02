package illyan.butler.services.chat.data.datasource

import illyan.butler.services.chat.data.model.chat.ResourceDto

interface ResourceDataSource {
    suspend fun createResource(userId: String, resource: ResourceDto): ResourceDto
    suspend fun getResource(userId: String, resourceId: String): ResourceDto?
    suspend fun deleteResource(userId: String, resourceId: String): Boolean
}
