package illyan.butler.repository.resource

import illyan.butler.domain.model.DomainResource

interface ResourceRepository {
    suspend fun getResource(resourceId: String): DomainResource?
    suspend fun createResource(resource: DomainResource): DomainResource
    suspend fun deleteResource(resourceId: String): Boolean
}