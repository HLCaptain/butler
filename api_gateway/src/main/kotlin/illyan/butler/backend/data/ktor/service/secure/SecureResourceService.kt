package illyan.butler.backend.data.ktor.service.secure

import illyan.butler.backend.data.model.chat.ResourceDto
import kotlinx.coroutines.flow.Flow

interface SecureResourceService : RPCWithJWT {
    suspend fun fetchNewResources(): Flow<List<ResourceDto>>
    suspend fun fetchResourceById(resourceId: String): Flow<ResourceDto>
    suspend fun upsert(resource: ResourceDto): ResourceDto
    suspend fun fetchByUser(): Flow<List<ResourceDto>>
    suspend fun delete(resourceId: String): Boolean
}