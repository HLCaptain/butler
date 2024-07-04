package illyan.butler.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import illyan.butler.data.room.model.RoomChat
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {
    @Insert
    suspend fun insertChat(chat: RoomChat): Long

    @Insert
    suspend fun insertChats(chats: List<RoomChat>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChat(chat: RoomChat): Long

    @Update
    suspend fun updateChat(chat: RoomChat): Int

    @Update
    suspend fun updateChats(chats: List<RoomChat>): Int

    @Delete
    suspend fun deleteChat(chat: RoomChat)

    @Delete
    suspend fun deleteChats(chats: List<RoomChat>)

    @Query("DELETE FROM chats")
    suspend fun deleteChats()

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteChat(id: String)

    @Query("DELETE FROM chats WHERE id IN(:ids)")
    suspend fun deleteChatsWithIds(ids: List<String>)

    @Query("SELECT * FROM chats WHERE id = :id")
    fun getChatById(id: String): Flow<RoomChat>

    @Query("SELECT * FROM chats WHERE id IN(:ids)")
    fun getChatsById(ids: List<String>): Flow<List<RoomChat>>
}