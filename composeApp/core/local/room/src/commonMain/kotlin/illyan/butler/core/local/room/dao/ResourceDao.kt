package illyan.butler.core.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Insert
    suspend fun insertResource(resource: illyan.butler.core.local.room.model.RoomResource)

    @Insert
    suspend fun insertResources(resources: List<illyan.butler.core.local.room.model.RoomResource>)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertResource(resource: illyan.butler.core.local.room.model.RoomResource)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertResources(resources: List<illyan.butler.core.local.room.model.RoomResource>)

    @Transaction
    suspend fun replaceResource(oldResourceId: String, newResource: illyan.butler.core.local.room.model.RoomResource) {
        deleteResourceById(oldResourceId)
        insertResource(newResource)
    }

    @Update
    suspend fun updateResource(resource: illyan.butler.core.local.room.model.RoomResource)

    @Update
    suspend fun updateResources(resources: List<illyan.butler.core.local.room.model.RoomResource>)

    @Delete
    suspend fun deleteResource(resource: illyan.butler.core.local.room.model.RoomResource)

    @Query("DELETE FROM resource WHERE id = :id")
    suspend fun deleteResourceById(id: String)

    @Query("DELETE FROM resource")
    suspend fun deleteAllResources()

    @Query("SELECT * FROM resource WHERE id = :id")
    fun getResourceById(id: String): Flow<illyan.butler.core.local.room.model.RoomResource?>

    @Query("SELECT * FROM resource WHERE id IN(:ids)")
    fun getResourcesByIds(ids: List<String>): Flow<List<illyan.butler.core.local.room.model.RoomResource>>
}
