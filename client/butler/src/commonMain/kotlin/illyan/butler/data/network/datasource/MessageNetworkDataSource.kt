package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessageNetworkDataSource {
    fun fetch(uuid: String): Flow<MessageDto>
    fun fetchByChat(chatUUID: String): Flow<List<MessageDto>>
    suspend fun upsert(message: MessageDto)
    suspend fun delete(uuid: String)
    suspend fun deleteForChat(chatUUID: String)
}