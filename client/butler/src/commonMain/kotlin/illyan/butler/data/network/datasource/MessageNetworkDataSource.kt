package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.MessageDto

interface MessageNetworkDataSource {
    suspend fun fetch(uuid: String): MessageDto
    suspend fun fetchByChat(chatUUID: String): List<MessageDto>
    suspend fun upsert(message: MessageDto): MessageDto
    suspend fun delete(uuid: String): Boolean
    suspend fun deleteForChat(chatUUID: String): Boolean
}