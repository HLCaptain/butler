package illyan.butler.core.local.sqldelight.datasource

import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.local.model.DataHistory
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import io.github.aakira.napier.Napier
import org.koin.core.annotation.Single

@Single
class DataHistorySqlDelightDataSource(
    private val database: ButlerDatabase
) : DataHistoryLocalDataSource {

    override suspend fun getLastFailedTimestamp(key: String): Long? {
        Napier.d { "Getting last failed timestamp for key: $key" }
        return database.dataHistoryQueries.selectLastFailedTimestamp(key).executeAsOneOrNull()
    }

    override suspend fun insertDataHistory(dataHistory: DataHistory) {
        Napier.d { "Inserting data history for key: ${dataHistory.key}" }
        database.dataHistoryQueries.insertOrReplaceDataHistory(
            key = dataHistory.key,
            group_name = dataHistory.group,
            lastFailedTimestamp = dataHistory.lastFailedTimestamp
        )
    }

    override suspend fun deleteDataHistory(key: String) {
        Napier.d { "Deleting data history for key: $key" }
        database.dataHistoryQueries.deleteDataHistory(key)
    }

    override suspend fun deleteDataHistoryByGroup(group: String) {
        Napier.d { "Deleting data history for group: $group" }
        database.dataHistoryQueries.deleteDataHistoryByGroup(group)
    }
}
