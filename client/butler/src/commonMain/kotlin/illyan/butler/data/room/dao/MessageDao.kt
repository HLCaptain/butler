package illyan.butler.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import illyan.butler.data.room.model.RoomMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: RoomMessage): Long

    @Insert
    suspend fun insertMessages(messages: List<RoomMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: RoomMessage): Long

    @Update
    suspend fun updateMessage(message: RoomMessage): Int

    @Update
    suspend fun updateMessages(messages: List<RoomMessage>): Int

    @Delete
    suspend fun deleteMessage(message: RoomMessage)

    @Delete
    suspend fun deleteMessages(messages: List<RoomMessage>)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteAllChatMessagesForChat(chatId: String)

    @Query("DELETE FROM messages WHERE senderId = :senderId")
    suspend fun deleteBySender(senderId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("SELECT * FROM messages WHERE id = :id")
    fun getMessageById(id: String): Flow<RoomMessage>

    @Query("SELECT * FROM messages WHERE chatId = :chatId")
    fun getMessagesByChatId(chatId: String): Flow<List<RoomMessage>>

    @Query("SELECT * FROM messages WHERE senderId = :senderId")
    fun getMessagesBySenderId(senderId: String): Flow<List<RoomMessage>>
}