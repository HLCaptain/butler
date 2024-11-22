package illyan.butler.core.local.datasource

import illyan.butler.core.local.model.DataHistory

interface DataHistoryLocalDataSource {
    suspend fun getLastFailedTimestamp(key: String): Long?
    suspend fun insertDataHistory(dataHistory: DataHistory)
    suspend fun deleteDataHistory(key: String)
    suspend fun deleteDataHistoryByGroup(group: String)
}