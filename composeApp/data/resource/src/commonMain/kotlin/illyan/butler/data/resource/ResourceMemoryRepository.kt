package illyan.butler.data.resource

import illyan.butler.domain.model.DomainResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class ResourceMemoryRepository : ResourceRepository {
    val resources = mutableMapOf<String, MutableStateFlow<Pair<DomainResource?, Boolean>>>()
    override fun getResourceFlow(resourceId: String): StateFlow<Pair<DomainResource?, Boolean>> {
        return resources.getOrPut(resourceId) { MutableStateFlow(null to true) }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun upsert(resource: DomainResource): String {
        val resourceWithId = if (resource.id == null) resource.copy(id = Uuid.random().toString()) else resource
        resources.getOrPut(resourceWithId.id!!) { MutableStateFlow(null to true) }.update { resourceWithId to false }
        return resourceWithId.id!!
    }

    override suspend fun deleteAllResources() {
        resources.values.forEach { it.update { null to false } }
        resources.clear()
    }

    override suspend fun deleteResource(resourceId: String): Boolean {
        return resources.remove(resourceId) != null
    }
}