package illyan.butler.server.data.datasource

import illyan.butler.shared.model.chat.ResourceDto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ResourceDataSource {
    suspend fun createResource(resource: ResourceDto): ResourceDto
    suspend fun getResource(userId: Uuid, resourceId: Uuid): ResourceDto?
    suspend fun deleteResource(userId: Uuid, resourceId: Uuid): Boolean
    suspend fun getResources(userId: Uuid): List<ResourceDto>
}
