package illyan.butler.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import illyan.butler.data.room.model.RoomResource
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {
    @Insert
    suspend fun insertResource(resource: RoomResource)

    @Insert
    suspend fun insertResources(resources: List<RoomResource>)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertResource(resource: RoomResource)

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun upsertResources(resources: List<RoomResource>)

    @Transaction
    suspend fun replaceResource(oldResourceId: String, newResource: RoomResource) {
        deleteResourceById(oldResourceId)
        insertResource(newResource)
    }

    @Update
    suspend fun updateResource(resource: RoomResource)

    @Update
    suspend fun updateResources(resources: List<RoomResource>)

    @Delete
    suspend fun deleteResource(resource: RoomResource)

    @Query("DELETE FROM resource WHERE id = :id")
    suspend fun deleteResourceById(id: String)

    @Query("DELETE FROM resource")
    suspend fun deleteAllResources()

    @Query("SELECT * FROM resource WHERE id = :id")
    fun getResourceById(id: String): Flow<RoomResource?>

    @Query("SELECT * FROM resource WHERE id IN(:ids)")
    fun getResourcesByIds(ids: List<String>): Flow<List<RoomResource>>
}
