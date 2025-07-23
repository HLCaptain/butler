package illyan.butler.server.data.db

import illyan.butler.shared.model.chat.ResourceDto
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ResourceDatabase {
    suspend fun createResource(resource: ResourceDto): ResourceDto
    suspend fun getResource(userId: Uuid, resourceId: Uuid): ResourceDto
    fun getResourceFlow(userId: Uuid, resourceId: Uuid): Flow<ResourceDto>
    suspend fun deleteResource(userId: Uuid, resourceId: Uuid): Boolean
    suspend fun getResources(userId: Uuid): List<ResourceDto>
    fun getResourcesFlow(userId: Uuid): Flow<List<ResourceDto>>
}
