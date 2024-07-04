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

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChats(chats: List<RoomChat>): List<Long>

    @Update
    suspend fun updateChat(chat: RoomChat): Int

    @Update
    suspend fun updateChats(chats: List<RoomChat>): Int

    @Delete
    suspend fun deleteChatById(chat: RoomChat)

    @Delete
    suspend fun deleteAllChats(chats: List<RoomChat>)

    @Query("DELETE FROM chats")
    suspend fun deleteAllChats()

    @Query("DELETE FROM chats WHERE id = :id")
    suspend fun deleteChatById(id: String)

    @Query("DELETE FROM chats WHERE id IN(:ids)")
    suspend fun deleteChatsWithIds(ids: List<String>)

    @Query("DELETE FROM chats WHERE id IN (SELECT chatId FROM chat_members WHERE userId = :userId)")
    suspend fun deleteChatsByUserId(userId: String)

    @Query("SELECT * FROM chats WHERE id = :id")
    fun getChatById(id: String): Flow<RoomChat>

    @Query("SELECT chats.* FROM chats JOIN chat_members ON chats.id = chat_members.chatId WHERE chat_members.userId = :userId")
    fun getChatsByUser(userId: String): Flow<List<RoomChat>>

    @Query("SELECT * FROM chats WHERE id IN(:ids)")
    fun getChatsById(ids: List<String>): Flow<List<RoomChat>>
}