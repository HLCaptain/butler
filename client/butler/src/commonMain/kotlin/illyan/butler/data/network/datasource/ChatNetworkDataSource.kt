package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.ChatDto

interface ChatNetworkDataSource {
    suspend fun fetchChat(uuid: String): ChatDto
    suspend fun fetchChatsByUser(userUUID: String): List<ChatDto>
    suspend fun fetchChatsByModel(modelUUID: String): List<ChatDto>
    suspend fun upsertChat(chat: ChatDto): ChatDto
    suspend fun deleteChat(uuid: String): Boolean
    suspend fun deleteChatsForUser(userUUID: String): Boolean
}