package illyan.butler.data.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import illyan.butler.data.room.model.RoomChatMember
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMemberDao {
    @Insert
    suspend fun insertChatMember(chatMember: RoomChatMember): Long

    @Insert
    suspend fun insertChatMembers(chatMembers: List<RoomChatMember>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChatMember(chatMember: RoomChatMember): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertChatMembers(chatMembers: List<RoomChatMember>): List<Long>

    @Delete
    suspend fun deleteChatMember(chatMember: RoomChatMember)

    @Delete
    suspend fun deleteChatMembers(chatMembers: List<RoomChatMember>)

    @Query("DELETE FROM chat_members WHERE chatId = :chatId")
    suspend fun deleteChatMembersByChatId(chatId: String)

    @Query("DELETE FROM chat_members WHERE chatId IN(:chatIds)")
    suspend fun deleteChatMembersForChat(chatIds: List<String>)

    @Query("DELETE FROM chat_members")
    suspend fun deleteAllChatMembers()

    @Query("SELECT * FROM chat_members WHERE chatId = :chatId")
    fun getChatMembersByChatId(chatId: String): Flow<List<RoomChatMember>>

    @Query("SELECT chatId FROM chat_members WHERE userId = :userId")
    fun getUserChatIds(userId: String): Flow<List<String>>
}
