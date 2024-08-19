package illyan.butler.data.ktor.rpc.service

import illyan.butler.data.network.model.chat.ResourceDto
import kotlinx.coroutines.flow.Flow
import kotlinx.rpc.RPC

interface ResourceService : RPC {
    suspend fun fetchNewResources(): Flow<List<ResourceDto>>
    suspend fun fetchResourceById(resourceId: String): Flow<ResourceDto>
    suspend fun upsert(resource: ResourceDto): ResourceDto
    suspend fun fetchByUser(): Flow<List<ResourceDto>>
    suspend fun delete(resourceId: String): Boolean
}