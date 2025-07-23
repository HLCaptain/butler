package illyan.butler.server.data.db

import illyan.butler.server.data.utils.getLastMonthDate
import illyan.butler.server.data.utils.getLastWeekDate
import illyan.butler.shared.model.chat.ChatDto
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
interface ChatDatabase {
    suspend fun getChat(userId: Uuid, chatId: Uuid): ChatDto
    fun getChatFlow(userId: Uuid, chatId: Uuid): Flow<ChatDto>
    suspend fun createChat(userId: Uuid, chat: ChatDto): ChatDto
    suspend fun editChat(userId: Uuid, chat: ChatDto): ChatDto
    suspend fun deleteChat(userId: Uuid, chatId: Uuid): Boolean
    suspend fun getChatsLastMonth(userId: Uuid) = getChats(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds()
    )
    fun getChatsLastMonthFlow(userId: Uuid): Flow<List<ChatDto>>
    suspend fun getChatsLastWeek(userId: Uuid) = getChats(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds()
    )
    fun getChatsLastWeekFlow(userId: Uuid): Flow<List<ChatDto>>
    suspend fun getChats(userId: Uuid): List<ChatDto>
    fun getChatsFlow(userId: Uuid): Flow<List<ChatDto>>
    suspend fun getChats(userId: Uuid, limit: Int, offset: Int): List<ChatDto>
    fun getChatsFlow(userId: Uuid, limit: Int, offset: Int): Flow<List<ChatDto>>
    suspend fun getChats(userId: Uuid, fromDate: Long, toDate: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()): List<ChatDto>
    fun getChatsFlow(userId: Uuid, fromDate: Long, toDate: Long = kotlin.time.Clock.System.now().toEpochMilliseconds()): Flow<List<ChatDto>>
    suspend fun getPreviousChats(userId: Uuid, limit: Int, timestamp: Long): List<ChatDto>
    fun getPreviousChatsFlow(userId: Uuid, limit: Int, timestamp: Long): Flow<List<ChatDto>>
    suspend fun getPreviousChats(userId: Uuid, limit: Int, offset: Int): List<ChatDto>
    fun getPreviousChatsFlow(userId: Uuid, limit: Int, offset: Int): Flow<List<ChatDto>>
    fun getChangedChatsAffectingUser(userId: Uuid): Flow<List<ChatDto>>
    fun getChangesFromChat(userId: Uuid, chatId: Uuid): Flow<ChatDto>
}
