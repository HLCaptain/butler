package illyan.butler.data.sqldelight.datasource

import illyan.butler.data.local.datasource.ResourceLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.domain.model.DomainResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ResourceSqlDelightDataSource(private val databaseHelper: DatabaseHelper) : ResourceLocalDataSource {
    override fun getResource(key: String): Flow<DomainResource?> {
        return databaseHelper.queryAsOneOrNullFlow { it.resourceQueries.select(key) }.map { it?.toDomainModel() }
    }

    override suspend fun upsertResource(resource: DomainResource) {
        databaseHelper.withDatabase { it.resourceQueries.upsert(resource.toLocalModel()) }
    }

    override suspend fun deleteResource(key: String) {
        databaseHelper.withDatabase { it.resourceQueries.delete(key) }
    }

    override suspend fun deleteAllResources() {
        databaseHelper.withDatabase { it.resourceQueries.deleteAll() }
    }
}