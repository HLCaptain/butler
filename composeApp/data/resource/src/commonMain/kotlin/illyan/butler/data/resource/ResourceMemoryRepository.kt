package illyan.butler.data.resource

import illyan.butler.domain.model.Resource
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class ResourceMemoryRepository : ResourceRepository {
    val resources = mutableSetOf<Resource>()

    override suspend fun delete(resource: Resource) {
        resources.remove(resource)
    }

    override fun getResourceFlow(
        resourceId: Uuid,
        source: Source
    ): Flow<Resource?> {
        val resourceFlow = MutableStateFlow<Resource?>(null)
        resourceFlow.update {
            resources.find { it.id == resourceId && it.source == source }
        }
        return resourceFlow
    }

    override suspend fun upsert(resource: Resource): Uuid {
        resources.removeIf { it.id == resource.id }
        resources.add(resource)
        return resource.id
    }

    override suspend fun create(resource: Resource): Uuid {
        resources.add(resource)
        return resource.id
    }
}
