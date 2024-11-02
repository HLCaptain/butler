package illyan.butler.repository.resource

import illyan.butler.model.DomainResource
import illyan.butler.utils.randomUUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class ResourceMemoryRepository : ResourceRepository {
    val resources = mutableMapOf<String, MutableStateFlow<Pair<DomainResource?, Boolean>>>()
    override fun getResourceFlow(resourceId: String): StateFlow<Pair<DomainResource?, Boolean>> {
        return resources.getOrPut(resourceId) { MutableStateFlow(null to true) }
    }

    override suspend fun upsert(resource: DomainResource): String {
        val resourceWithId = if (resource.id == null) resource.copy(id = randomUUID()) else resource
        resources.getOrPut(resourceWithId.id!!) { MutableStateFlow(null to true) }.update { resourceWithId to false }
        return resourceWithId.id
    }

    override suspend fun deleteAllResources() {
        resources.values.forEach { it.update { null to false } }
        resources.clear()
    }

    override suspend fun deleteResource(resourceId: String): Boolean {
        return resources.remove(resourceId) != null
    }
}