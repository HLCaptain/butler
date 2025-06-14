package illyan.butler.core.local.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import illyan.butler.core.local.room.model.RoomMessage
import illyan.butler.domain.model.SenderType
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Dao
interface MessageDao {
    @Insert
    suspend fun insertMessage(message: RoomMessage): Long

    @Insert
    suspend fun insertMessages(messages: List<RoomMessage>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessage(message: RoomMessage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMessages(messages: List<RoomMessage>): List<Long>

    @Transaction
    suspend fun replaceMessage(oldMessageId: Uuid, newMessage: RoomMessage) {
        deleteMessageById(oldMessageId)
        insertMessage(newMessage)
    }

    @Update
    suspend fun updateMessage(message: RoomMessage): Int

    @Update
    suspend fun updateMessages(messages: List<RoomMessage>): Int

    @Delete
    suspend fun deleteMessage(message: RoomMessage)

    @Delete
    suspend fun deleteMessages(messages: List<RoomMessage>)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: Uuid)

    @Query("DELETE FROM messages WHERE chatId = :chatId")
    suspend fun deleteAllChatMessagesForChat(chatId: Uuid)

    @Query("DELETE FROM messages WHERE sender = :sender")
    suspend fun deleteBySender(sender: SenderType)

    @Query("DELETE FROM messages")
    suspend fun deleteAllMessages()

    @Query("SELECT * FROM messages WHERE id = :id")
    fun getMessageById(id: Uuid): Flow<RoomMessage?>

    @Query("SELECT * FROM messages WHERE chatId = :chatId")
    fun getMessagesByChatId(chatId: Uuid): Flow<List<RoomMessage>>

    @Query("SELECT * FROM messages WHERE sender = :sender")
    fun getMessagesBySenderId(sender: SenderType): Flow<List<RoomMessage>>

    @Query("SELECT messages.* FROM messages JOIN chats ON messages.chatId = chats.id WHERE chats.ownerId = :userId")
    fun getAccessibleMessagesForUser(userId: Uuid): Flow<List<RoomMessage>>
}