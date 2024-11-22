package illyan.butler.server.data.datasource

import illyan.butler.server.data.utils.getLastMonthDate
import illyan.butler.server.data.utils.getLastWeekDate
import illyan.butler.shared.model.chat.ChatDto
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

interface ChatDataSource {
    suspend fun getChat(userId: String, chatId: String): ChatDto
    suspend fun createChat(userId: String, chat: ChatDto): ChatDto
    suspend fun editChat(userId: String, chat: ChatDto)
    suspend fun deleteChat(userId: String, chatId: String): Boolean
    suspend fun getChatsLastMonth(userId: String) = getChats(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds()
    )
    suspend fun getChatsLastWeek(userId: String) = getChats(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds()
    )
    suspend fun getChats(userId: String): List<ChatDto>
    suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto>
    suspend fun getChats(userId: String, fromDate: Long, toDate: Long = Clock.System.now().toEpochMilliseconds()): List<ChatDto>
    suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto>
    suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto>
    fun getChangedChatsAffectingUser(userId: String): Flow<List<ChatDto>>
    fun getChangesFromChat(userId: String, chatId: String): Flow<ChatDto>
}