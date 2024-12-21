package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.local.model.DataHistory
import illyan.butler.core.local.room.dao.DataHistoryDao
import illyan.butler.core.local.room.model.RoomDataHistory
import org.koin.core.annotation.Single

@Single
class DataHistoryRoomDataSource(
    private val dataHistoryDao: DataHistoryDao
) : DataHistoryLocalDataSource {
    override suspend fun getLastFailedTimestamp(key: String): Long? {
        return dataHistoryDao.getDataHistoryByKey(key)
    }

    override suspend fun insertDataHistory(dataHistory: DataHistory) {
        dataHistoryDao.insertDataHistory(
            RoomDataHistory(
                dataHistory.key,
                dataHistory.lastFailedTimestamp,
                dataHistory.group
            )
        )
    }

    override suspend fun deleteDataHistory(key: String) {
        dataHistoryDao.deleteDataHistoryByKey(key)
    }

    override suspend fun deleteDataHistoryByGroup(group: String) {
        dataHistoryDao.deleteDataHistoryByGroup(group)
    }
}
