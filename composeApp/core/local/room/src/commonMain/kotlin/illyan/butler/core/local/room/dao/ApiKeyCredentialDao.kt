package illyan.butler.core.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import illyan.butler.core.local.room.model.RoomApiKeyCredential
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiKeyCredentialDao {
    @Insert
    suspend fun insertApiKeyCredential(apiKeyCredential: RoomApiKeyCredential)

    @Insert
    suspend fun insertApiKeyCredentials(apiKeyCredentials: List<RoomApiKeyCredential>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApiKeyCredential(apiKeyCredential: RoomApiKeyCredential)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApiKeyCredentials(apiKeyCredentials: List<RoomApiKeyCredential>)

    @Update
    suspend fun updateApiKeyCredential(apiKeyCredential: RoomApiKeyCredential): Int

    @Update
    suspend fun updateApiKeyCredentials(apiKeyCredentials: List<RoomApiKeyCredential>): Int

    @Query("DELETE FROM api_key_credentials")
    suspend fun deleteAllApiKeyCredentials()

    @Query("DELETE FROM api_key_credentials WHERE providerUrl = :providerUrl")
    suspend fun deleteApiKeyCredentialsByProviderUrl(providerUrl: String)

    @Query("SELECT * FROM api_key_credentials WHERE providerUrl = :providerUrl LIMIT 1")
    fun getApiKeyCredentialsByProviderUrl(providerUrl: String): Flow<RoomApiKeyCredential?>

    @Query("SELECT * FROM api_key_credentials")
    fun getAllApiKeyCredentials(): Flow<List<RoomApiKeyCredential>>
}
