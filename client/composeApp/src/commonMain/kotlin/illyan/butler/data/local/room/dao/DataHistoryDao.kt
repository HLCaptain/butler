package illyan.butler.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import illyan.butler.data.local.room.model.RoomDataHistory

@Dao
interface DataHistoryDao {
    @Insert
    suspend fun insertDataHistory(dataHistory: RoomDataHistory)

    @Delete
    suspend fun deleteDataHistory(dataHistory: RoomDataHistory)

    @Update
    suspend fun updateDataHistory(dataHistory: RoomDataHistory)

    @Query("DELETE FROM data_history WHERE `key` = :key")
    suspend fun deleteDataHistoryByKey(key: String)

    @Query("SELECT lastFailedTimestamp FROM data_history WHERE `key` = :key")
    suspend fun getDataHistoryByKey(key: String): Long?

    @Query("DELETE FROM data_history WHERE `group` = :group")
    suspend fun deleteDataHistoryByGroup(group: String)
}