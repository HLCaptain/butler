package illyan.butler.data.resource

import illyan.butler.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class ResourceMemoryRepository : ResourceRepository {
    val resources = mutableMapOf<String, MutableStateFlow<Resource?>>()
    override fun getResourceFlow(resourceId: String, deviceOnly: Boolean): Flow<Resource?> {
        return resources.getOrPut(resourceId) { MutableStateFlow(null) }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun upsert(resource: Resource, deviceOnly: Boolean): String {
        val resourceWithId = if (resource.id == null) resource.copy(id = Uuid.random().toString()) else resource
        resources.getOrPut(resourceWithId.id!!) { MutableStateFlow(null) }.update { resourceWithId }
        return resourceWithId.id!!
    }

    override suspend fun deleteResource(resource: Resource): Boolean {
        return resources.remove(resourceId) != null
    }
}
