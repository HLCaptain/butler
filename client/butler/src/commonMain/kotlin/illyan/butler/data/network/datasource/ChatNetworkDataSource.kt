package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.ChatDto

interface ChatNetworkDataSource {
    suspend fun fetch(uuid: String): ChatDto
    suspend fun fetchByUser(userUUID: String): List<ChatDto>
    suspend fun fetchByModel(modelUUID: String): List<ChatDto>
    suspend fun upsert(chat: ChatDto): ChatDto
    suspend fun delete(uuid: String): Boolean
    suspend fun deleteForUser(userUUID: String): Boolean
}