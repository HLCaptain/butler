package illyan.butler.services.chat.data.db

import illyan.butler.services.chat.data.model.chat.ResourceDto

interface ResourceDatabase {
    suspend fun createResource(userId: String, messageId: String, resource: ResourceDto): ResourceDto
    suspend fun getResource(userId: String, resourceId: String): ResourceDto?
    suspend fun deleteResource(userId: String, resourceId: String): Boolean
}
