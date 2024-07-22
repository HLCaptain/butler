package illyan.butler.data.room.datasource

import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.model.DataHistory
import illyan.butler.data.room.dao.DataHistoryDao
import illyan.butler.data.room.model.RoomDataHistory
import org.koin.core.annotation.Single

@Single
class DataHistoryRoomDataSource(
    private val dataHistoryDao: DataHistoryDao
) : DataHistoryLocalDataSource {
    override suspend fun getLastFailedTimestamp(key: String): Long? {
        return dataHistoryDao.getDataHistoryByKey(key)
    }

    override suspend fun insertDataHistory(dataHistory: DataHistory) {
        dataHistoryDao.insertDataHistory(RoomDataHistory(dataHistory.key, dataHistory.lastFailedTimestamp, dataHistory.group))
    }

    override suspend fun deleteDataHistory(key: String) {
        dataHistoryDao.deleteDataHistoryByKey(key)
    }

    override suspend fun deleteDataHistoryByGroup(group: String) {
        dataHistoryDao.deleteDataHistoryByGroup(group)
    }
}
