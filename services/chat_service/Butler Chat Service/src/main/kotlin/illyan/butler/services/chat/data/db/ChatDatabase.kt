package illyan.butler.services.chat.data.db

import illyan.butler.services.chat.data.model.chat.ChatDto

interface ChatDatabase {
    suspend fun getChat(userId: String, chatId: String): ChatDto
    suspend fun createChat(userId: String, chat: ChatDto): ChatDto
    suspend fun editChat(userId: String, chat: ChatDto)
    suspend fun deleteChat(userId: String, chatId: String)
    suspend fun getChatsLastMonth(userId: String): List<ChatDto>
    suspend fun getChatsLastWeek(userId: String): List<ChatDto>
    suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto>
    suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto>
    suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto>
}