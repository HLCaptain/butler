package illyan.butler.server.data.datasource

import illyan.butler.server.data.utils.getLastMonthDate
import illyan.butler.server.data.utils.getLastWeekDate
import illyan.butler.shared.model.chat.ChatDto
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
interface ChatDataSource {
    suspend fun getChat(userId: Uuid, chatId: Uuid): ChatDto
    suspend fun createChat(userId: Uuid, chat: ChatDto): ChatDto
    suspend fun editChat(userId: Uuid, chat: ChatDto): ChatDto
    suspend fun deleteChat(userId: Uuid, chatId: Uuid): Boolean
    suspend fun getChatsLastMonth(userId: Uuid) = getChats(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds()
    )
    suspend fun getChatsLastWeek(userId: Uuid) = getChats(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds()
    )
    suspend fun getChats(userId: Uuid): List<ChatDto>
    suspend fun getChats(userId: Uuid, limit: Int, offset: Int): List<ChatDto>
    suspend fun getChats(userId: Uuid, fromDate: Long, toDate: Long = Clock.System.now().toEpochMilliseconds()): List<ChatDto>
    suspend fun getPreviousChats(userId: Uuid, limit: Int, timestamp: Long): List<ChatDto>
    suspend fun getPreviousChats(userId: Uuid, limit: Int, offset: Int): List<ChatDto>
    fun getChangedChatsAffectingUser(userId: Uuid): Flow<List<ChatDto>>
    fun getChangesFromChat(userId: Uuid, chatId: Uuid): Flow<ChatDto>
}
