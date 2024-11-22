package illyan.butler.core.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import illyan.butler.core.local.room.model.RoomToken
import illyan.butler.core.local.room.model.RoomUser
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: RoomUser)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers(): Int

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<RoomUser?>

    @Query("SELECT COUNT(*) FROM users")
    fun isUserSignedIn(): Flow<Boolean>

    @Query("UPDATE users SET accessToken = :accessToken, refreshToken = :refreshToken")
    suspend fun updateTokens(accessToken: RoomToken?, refreshToken: RoomToken?): Int
}