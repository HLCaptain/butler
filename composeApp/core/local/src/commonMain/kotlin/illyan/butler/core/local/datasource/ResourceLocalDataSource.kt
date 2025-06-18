package illyan.butler.core.local.datasource

import illyan.butler.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface ResourceLocalDataSource {
    fun getResource(resourceId: Uuid): Flow<Resource?>
    suspend fun replaceResource(oldResourceId: Uuid, newResource: Resource)
    suspend fun upsertResource(resource: Resource)
    suspend fun deleteResourceById(resourceId: Uuid)
    suspend fun deleteAllResources()
}