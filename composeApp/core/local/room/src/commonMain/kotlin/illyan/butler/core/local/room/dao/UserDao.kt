@file:OptIn(ExperimentalUuidApi::class)

package illyan.butler.core.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import illyan.butler.core.local.room.model.RoomToken
import illyan.butler.core.local.room.model.RoomUser
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: RoomUser)

    @Query("DELETE FROM users")
    suspend fun deleteAllUsers(): Int

    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUser(userId: String): Int

    @Query("SELECT * FROM users")
    fun getUsers(): Flow<List<RoomUser>>

    @Query("SELECT * FROM users WHERE id = :userId")
    fun getUser(userId: String): Flow<RoomUser?>

    @Query("UPDATE users SET accessToken = :accessToken, refreshToken = :refreshToken WHERE id = :userId")
    suspend fun updateTokens(userId: String, accessToken: RoomToken?, refreshToken: RoomToken?): Int
}
