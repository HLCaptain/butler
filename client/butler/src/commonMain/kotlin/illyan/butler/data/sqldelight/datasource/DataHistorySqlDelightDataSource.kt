package illyan.butler.data.sqldelight.datasource

import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.model.DataHistory
import illyan.butler.data.sqldelight.DatabaseHelper
import kotlinx.coroutines.flow.firstOrNull
import org.koin.core.annotation.Single

@Single
class DataHistorySqlDelightDataSource(private val databaseHelper: DatabaseHelper) : DataHistoryLocalDataSource {
    override suspend fun getLastFailedTimestamp(key: String): Long? {
        return databaseHelper.queryAsOneOrNullFlow { it.dataHistoryQueries.select(key) }.firstOrNull()?.timestamp
    }

    override suspend fun insertDataHistory(dataHistory: DataHistory) {
        databaseHelper.withDatabase {
            it.dataHistoryQueries.upsert(illyan.butler.db.DataHistory(dataHistory.key, dataHistory.lastFailedTimestamp, dataHistory.group))
        }
    }

    override suspend fun deleteDataHistory(key: String) {
        databaseHelper.withDatabase {
            it.dataHistoryQueries.delete(key)
        }
    }

    override suspend fun deleteDataHistoryByGroup(group: String) {
        databaseHelper.withDatabase {
            it.dataHistoryQueries.deleteAllFromTable(group)
        }
    }
}