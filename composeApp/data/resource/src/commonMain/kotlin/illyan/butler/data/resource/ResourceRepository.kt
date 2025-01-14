package illyan.butler.data.resource

import illyan.butler.domain.model.DomainResource
import kotlinx.coroutines.flow.Flow

interface ResourceRepository {
    fun getResourceFlow(resourceId: String, deviceOnly: Boolean): Flow<DomainResource?>
    suspend fun upsert(resource: DomainResource, deviceOnly: Boolean): String
    suspend fun deleteResource(resourceId: String, deviceOnly: Boolean): Boolean
}
