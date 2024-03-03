package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.MessageDto

interface MessageNetworkDataSource {
    suspend fun fetchMessage(uuid: String): MessageDto
    suspend fun fetchMessagesByChat(chatUUID: String): List<MessageDto>
    suspend fun upsertMessage(message: MessageDto): MessageDto
    suspend fun deleteMessage(uuid: String): Boolean
    suspend fun deleteMessagesForChat(chatUUID: String): Boolean
}