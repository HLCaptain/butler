package illyan.butler.core.network.datasource

import illyan.butler.domain.model.DomainResource
import illyan.butler.shared.model.chat.ResourceDto
import kotlinx.coroutines.flow.Flow

interface ResourceNetworkDataSource {
    fun fetchNewResources(): Flow<List<DomainResource>>
    fun fetchResourceById(resourceId: String): Flow<DomainResource>
    suspend fun upsert(resource: DomainResource): DomainResource
    fun fetchByUser(): Flow<List<DomainResource>>
    suspend fun delete(resourceId: String): Boolean
}