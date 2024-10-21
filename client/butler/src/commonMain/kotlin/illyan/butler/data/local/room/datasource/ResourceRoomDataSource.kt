package illyan.butler.data.local.room.datasource

import illyan.butler.data.local.datasource.ResourceLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toRoomModel
import illyan.butler.data.local.room.dao.ResourceDao
import illyan.butler.model.DomainResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ResourceRoomDataSource(private val resourceDao: ResourceDao) : ResourceLocalDataSource {
    override fun getResource(resourceId: String): Flow<DomainResource?> {
        return resourceDao.getResourceById(resourceId).map { it?.toDomainModel() }
    }

    override suspend fun upsertResource(resource: DomainResource) {
        resourceDao.upsertResource(resource.toRoomModel())
    }

    override suspend fun replaceResource(oldResourceId: String, newResource: DomainResource) {
        resourceDao
    }

    override suspend fun deleteResourceById(resourceId: String) {
        resourceDao.deleteResourceById(resourceId)
    }

    override suspend fun deleteAllResources() {
        resourceDao.deleteAllResources()
    }
}