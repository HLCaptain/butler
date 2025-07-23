package illyan.butler.core.local.sql.datasource

import illyan.butler.core.local.datasource.ResourceLocalDataSource
import illyan.butler.core.local.sql.DatabaseHelper
import illyan.butler.core.local.sql.mapping.toDomainModel
import illyan.butler.core.local.sql.mapping.toSqlDelightModel
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import illyan.butler.domain.model.Resource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class ResourceSqlDelightDataSource(
    private val databaseHelper: DatabaseHelper<ButlerDatabase>
) : ResourceLocalDataSource {

    override fun getResource(resourceId: Uuid): Flow<Resource?> {
        Napier.d { "Getting resource with id: $resourceId" }
        return databaseHelper.queryAsOneOrNullFlow {
            resourceQueries.selectResourceById(resourceId.toString())
        }.map { it?.toDomainModel() }
    }

    override suspend fun replaceResource(oldResourceId: Uuid, newResource: Resource) {
        Napier.d { "Replacing resource with id: $oldResourceId with new resource with id: ${newResource.id}" }
        databaseHelper.withDatabase {
            transaction {
                resourceQueries.deleteResource(oldResourceId.toString())
                val sqlDelightResource = newResource.toSqlDelightModel()
                resourceQueries.insertOrReplaceResource(
                    id = sqlDelightResource.id,
                    createdAt = sqlDelightResource.createdAt,
                    mimeType = sqlDelightResource.mimeType,
                    data_ = sqlDelightResource.data,
                    source = sqlDelightResource.source
                )
            }
        }
    }

    override suspend fun upsertResource(resource: Resource) {
        Napier.d { "Upserting resource with id: ${resource.id}" }
        val sqlDelightResource = resource.toSqlDelightModel()
        databaseHelper.withDatabase {
            resourceQueries.insertOrReplaceResource(
                id = sqlDelightResource.id,
                createdAt = sqlDelightResource.createdAt,
                mimeType = sqlDelightResource.mimeType,
                data_ = sqlDelightResource.data,
                source = sqlDelightResource.source
            )
        }
    }

    override suspend fun deleteResourceById(resourceId: Uuid) {
        Napier.d { "Deleting resource with id: $resourceId" }
        databaseHelper.withDatabase {
            resourceQueries.deleteResource(resourceId.toString())
        }
    }

    override suspend fun deleteAllResources() {
        Napier.d { "Deleting all resources" }
        databaseHelper.withDatabase {
            resourceQueries.deleteAllResources()
        }
    }
}
