package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessageNetworkDataSource {
    fun fetchNewMessages(): Flow<MessageDto>

    /**
     * Fetch messages from a chat.
     * @return messages in the chat.
     */
    suspend fun fetchByChat(chatUUID: String, limit: Int, timestamp: Long): List<MessageDto>

    suspend fun fetchByChat(chatUUID: String): List<MessageDto>

    /**
     * Update a message.
     * @return updated message.
     */
    suspend fun upsert(message: MessageDto): MessageDto

    /**
     * Delete a message.
     * @return true if the message is deleted.
     */
    suspend fun delete(id: String, chatId: String): Boolean
}