package illyan.butler.repository.resource

import illyan.butler.model.DomainResource
import kotlinx.coroutines.flow.StateFlow

interface ResourceRepository {
    fun getResourceFlow(resourceId: String): StateFlow<Pair<DomainResource?, Boolean>>
    suspend fun upsert(resource: DomainResource): String
    suspend fun deleteResource(resourceId: String): Boolean
    suspend fun deleteAllResources()
}