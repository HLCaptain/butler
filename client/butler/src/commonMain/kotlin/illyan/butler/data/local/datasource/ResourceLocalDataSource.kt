package illyan.butler.data.local.datasource

import illyan.butler.domain.model.DomainResource
import kotlinx.coroutines.flow.Flow

interface ResourceLocalDataSource {
    fun getResource(key: String): Flow<DomainResource?>
    suspend fun upsertResource(resource: DomainResource)
    suspend fun deleteResource(key: String)
    suspend fun deleteAllResources()
}