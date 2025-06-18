package illyan.butler.core.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import illyan.butler.core.local.room.model.RoomUserTokens
import kotlinx.coroutines.flow.Flow

@Dao
interface UserTokensDao {
    @Insert
    suspend fun insertUserTokens(userTokens: RoomUserTokens)

    @Insert
    suspend fun insertUserTokens(userTokens: List<RoomUserTokens>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserTokens(userTokens: RoomUserTokens)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserTokens(userTokens: List<RoomUserTokens>)

    @Update
    suspend fun updateUserTokens(userTokens: RoomUserTokens): Int

    @Update
    suspend fun updateUserTokens(userTokens: List<RoomUserTokens>): Int

    @Query("DELETE FROM user_tokens")
    suspend fun deleteAllUserTokens()

    @Query("SELECT * FROM user_tokens")
    fun getAllUserTokens(): Flow<List<RoomUserTokens>>

    @Query("SELECT * FROM user_tokens WHERE userId = :userId")
    fun getUserTokensByUserId(userId: String): Flow<RoomUserTokens?>
}
