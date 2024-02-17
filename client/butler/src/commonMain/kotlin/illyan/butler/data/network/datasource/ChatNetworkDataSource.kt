package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.ChatDto
import kotlinx.coroutines.flow.Flow

interface ChatNetworkDataSource {
    fun fetch(uuid: String): Flow<ChatDto>
    fun fetchByUser(userUUID: String): Flow<List<ChatDto>>
    fun fetchByModel(modelUUID: String): Flow<List<ChatDto>>
    suspend fun upsert(chat: ChatDto)
    suspend fun delete(uuid: String)
    suspend fun deleteForUser(userUUID: String)
}