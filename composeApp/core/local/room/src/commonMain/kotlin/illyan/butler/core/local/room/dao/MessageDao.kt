package illyan.butler.data.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import illyan.butler.core.local.room.model.RoomMessage
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: illyan.butler.core.local.room.model.RoomMessage): Long

    @Insert
    suspend fun insertMessages(messages: List<illyan.butler.core.local.room.model.RoomMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: illyan.butler.core.local.room.model.RoomMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<illyan.butler.core.local.room.model.RoomMessage>): List<Long>

    @Transaction
    suspend fun replaceMessage(oldMessageId: String, newMessage: illyan.butler.core.local.room.model.RoomMessage) {
        deleteMessageById(oldMessageId)
        insertMessage(newMessage)
    }

    @Update
    suspend fun updateMessage(message: illyan.butler.core.local.room.model.RoomMessage): Int

    @Update
    suspend fun updateMessages(messages: List<illyan.butler.core.local.room.model.RoomMessage>): Int

    @Delete
    suspend fun deleteMessage(message: illyan.butler.core.local.room.model.RoomMessage)

    @Delete
    suspend fun deleteMessages(messages: List<illyan.butler.core.local.room.model.RoomMessage>)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteAllChatMessagesForChat(chatId: String)

    @Query("DELETE FROM messages WHERE senderId = :senderId")
    suspend fun deleteBySender(senderId: String)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("SELECT * FROM messages WHERE id = :id")
    fun getMessageById(id: String): Flow<illyan.butler.core.local.room.model.RoomMessage?>

    @Query("SELECT * FROM messages WHERE chatId = :chatId")
    fun getMessagesByChatId(chatId: String): Flow<List<illyan.butler.core.local.room.model.RoomMessage>>

    @Query("SELECT * FROM messages WHERE senderId = :senderId")
    fun getMessagesBySenderId(senderId: String): Flow<List<illyan.butler.core.local.room.model.RoomMessage>>

    @Query("SELECT messages.* FROM messages JOIN chat_members ON messages.chatId = chat_members.chatId WHERE chat_members.userId = :userId")
    fun getAccessibleMessagesForUser(userId: String): Flow<List<illyan.butler.core.local.room.model.RoomMessage>>
}