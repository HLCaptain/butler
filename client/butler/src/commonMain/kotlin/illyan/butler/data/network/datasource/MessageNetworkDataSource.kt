package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessageNetworkDataSource {
    fun fetch(uuid: String): Flow<MessageDto>

    /**
     * Fetch messages from a chat.
     * TODO: make this paginated.
     * @return messages in the chat.
     */
    suspend fun fetchByChat(chatUUID: String, limit: Int, timestamp: Long): List<MessageDto>

    /**
     * Update a message.
     * @return updated message.
     */
    suspend fun upsert(message: MessageDto): MessageDto

    /**
     * Delete a message.
     * @return true if the message is deleted.
     */
    suspend fun delete(uuid: String): Boolean
}