package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.ResourceLocalDataSource
import illyan.butler.core.local.room.dao.ResourceDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class ResourceRoomDataSource(private val resourceDao: ResourceDao) : ResourceLocalDataSource {
    override fun getResource(resourceId: Uuid): Flow<Resource?> {
        return resourceDao.getResourceById(resourceId).map { it?.toDomainModel() }
    }

    override suspend fun upsertResource(resource: Resource) {
        resourceDao.upsertResource(resource.toRoomModel())
    }

    override suspend fun replaceResource(oldResourceId: Uuid, newResource: Resource) {
        resourceDao.replaceResource(oldResourceId, newResource.toRoomModel())
    }

    override suspend fun deleteResourceById(resourceId: Uuid) {
        resourceDao.deleteResourceById(resourceId)
    }

    override suspend fun deleteAllResources() {
        resourceDao.deleteAllResources()
    }
}
