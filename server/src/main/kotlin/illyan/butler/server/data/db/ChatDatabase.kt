package illyan.butler.server.data.db

import illyan.butler.server.data.utils.getLastMonthDate
import illyan.butler.server.data.utils.getLastWeekDate
import illyan.butler.shared.model.chat.ChatDto
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

interface ChatDatabase {
    suspend fun getChat(userId: String, chatId: String): ChatDto
    fun getChatFlow(userId: String, chatId: String): Flow<ChatDto>
    suspend fun createChat(userId: String, chat: ChatDto): ChatDto
    suspend fun editChat(userId: String, chat: ChatDto): ChatDto
    suspend fun deleteChat(userId: String, chatId: String): Boolean
    suspend fun getChatsLastMonth(userId: String) = getChats(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds()
    )
    fun getChatsLastMonthFlow(userId: String): Flow<List<ChatDto>>
    suspend fun getChatsLastWeek(userId: String) = getChats(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds()
    )
    fun getChatsLastWeekFlow(userId: String): Flow<List<ChatDto>>
    suspend fun getChats(userId: String): List<ChatDto>
    fun getChatsFlow(userId: String): Flow<List<ChatDto>>
    suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto>
    fun getChatsFlow(userId: String, limit: Int, offset: Int): Flow<List<ChatDto>>
    suspend fun getChats(userId: String, fromDate: Long, toDate: Long = Clock.System.now().toEpochMilliseconds()): List<ChatDto>
    fun getChatsFlow(userId: String, fromDate: Long, toDate: Long = Clock.System.now().toEpochMilliseconds()): Flow<List<ChatDto>>
    suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto>
    fun getPreviousChatsFlow(userId: String, limit: Int, timestamp: Long): Flow<List<ChatDto>>
    suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto>
    fun getPreviousChatsFlow(userId: String, limit: Int, offset: Int): Flow<List<ChatDto>>
    fun getChangedChatsAffectingUser(userId: String): Flow<List<ChatDto>>
    fun getChangesFromChat(userId: String, chatId: String): Flow<ChatDto>
}
