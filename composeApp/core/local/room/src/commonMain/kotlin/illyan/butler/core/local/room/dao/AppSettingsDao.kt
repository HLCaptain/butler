package illyan.butler.core.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import illyan.butler.core.local.room.model.RoomAppSettings
import illyan.butler.core.local.room.model.RoomPreferences
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT COUNT(*) FROM app_settings")
    fun hasAppSettings(): Flow<Boolean?>

    @Query("SELECT * FROM app_settings LIMIT 1")
    fun getAppSettings(): Flow<RoomAppSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppSettings(appSettings: RoomAppSettings): Long

    @Transaction
    suspend fun upsertAppSettings(appSettings: RoomAppSettings) {
        deleteAppSettings()
        insertAppSettings(appSettings)
    }

    @Query("DELETE FROM app_settings")
    suspend fun deleteAppSettings(): Int

    @Query("UPDATE app_settings SET hostUrl = :hostUrl")
    suspend fun updateHostUrl(hostUrl: String): Int

    @Query("UPDATE app_settings SET firstSignInHappenedYet = :firstSignInHappenedYet")
    suspend fun updateFirstSignInHappenedYet(firstSignInHappenedYet: Boolean): Int

    @Query("UPDATE app_settings SET preferences = :preferences")
    suspend fun updatePreferences(preferences: RoomPreferences): Int
}
