package illyan.butler.data.local.datasource

import illyan.butler.model.DomainResource
import kotlinx.coroutines.flow.Flow

interface ResourceLocalDataSource {
    fun getResource(resourceId: String): Flow<DomainResource?>
    suspend fun replaceResource(oldResourceId: String, newResource: DomainResource)
    suspend fun upsertResource(resource: DomainResource)
    suspend fun deleteResourceById(resourceId: String)
    suspend fun deleteAllResources()
}