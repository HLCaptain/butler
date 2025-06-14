package illyan.butler.core.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import illyan.butler.core.local.room.model.RoomChat
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
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

    @Transaction
    suspend fun replaceChat(oldChatId: Uuid, newChat: RoomChat) {
        deleteChatById(oldChatId)
        insertChat(newChat)
    }

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
    suspend fun deleteChatById(id: Uuid)

    @Query("DELETE FROM chats WHERE id IN(:ids)")
    suspend fun deleteChatsWithIds(ids: List<Uuid>)

    @Query("DELETE FROM chats WHERE ownerId = :userId")
    suspend fun deleteChatsByUserId(userId: Uuid)

    @Query("SELECT * FROM chats WHERE id = :id")
    fun getChatById(id: Uuid): Flow<RoomChat?>

    @Query("SELECT * FROM chats WHERE ownerId = :userId")
    fun getChatsByUser(userId: Uuid): Flow<List<RoomChat>>

    @Query("SELECT * FROM chats WHERE id IN(:ids)")
    fun getChatsById(ids: List<Uuid>): Flow<List<RoomChat>>
}