package illyan.butler.data.resource

import illyan.butler.domain.model.DomainResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class ResourceMemoryRepository : ResourceRepository {
    val resources = mutableMapOf<String, MutableStateFlow<DomainResource?>>()
    override fun getResourceFlow(resourceId: String, deviceOnly: Boolean): Flow<DomainResource?> {
        return resources.getOrPut(resourceId) { MutableStateFlow(null) }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun upsert(resource: DomainResource, deviceOnly: Boolean): String {
        val resourceWithId = if (resource.id == null) resource.copy(id = Uuid.random().toString()) else resource
        resources.getOrPut(resourceWithId.id!!) { MutableStateFlow(null) }.update { resourceWithId }
        return resourceWithId.id!!
    }

    override suspend fun deleteAllResources() {
        resources.values.forEach { it.update { null } }
        resources.clear()
    }

    override suspend fun deleteResource(resourceId: String): Boolean {
        return resources.remove(resourceId) != null
    }
}
