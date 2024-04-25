package illyan.butler.repository.resource

import illyan.butler.domain.model.DomainResource
import illyan.butler.util.log.randomUUID
import org.koin.core.annotation.Single

@Single
class ResourceMemoryRepository : ResourceRepository {
    val resources = mutableMapOf<String, DomainResource>()
    override suspend fun getResource(resourceId: String): DomainResource? {
        return resources[resourceId]
    }

    override suspend fun createResource(resource: DomainResource): DomainResource {
        val resourceWithId = if (resource.id == null) resource.copy(id = randomUUID()) else resource
        resources[resourceWithId.id!!] = resourceWithId
        return resourceWithId
    }

    override suspend fun deleteResource(resourceId: String): Boolean {
        return resources.remove(resourceId) != null
    }
}