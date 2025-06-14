package illyan.butler.data.resource

import illyan.butler.domain.model.Resource
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ResourceRepository {
    fun getResourceFlow(resourceId: Uuid, source: Source): Flow<Resource?>
    suspend fun upsert(resource: Resource): Uuid
    suspend fun deleteResource(resource: Resource)
}
