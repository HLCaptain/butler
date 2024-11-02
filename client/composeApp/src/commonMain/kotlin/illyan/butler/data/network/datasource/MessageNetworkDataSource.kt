package illyan.butler.data.network.datasource

import illyan.butler.data.network.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessageNetworkDataSource {
    fun fetchNewMessages(): Flow<List<MessageDto>>

    fun fetchByChatId(chatId: String): Flow<List<MessageDto>>

    /**
     * Update a message.
     * @return updated message.
     */
    suspend fun upsert(message: MessageDto): MessageDto

    /**
     * Delete a message.
     * @return true if the message is deleted.
     */
    suspend fun delete(messageId: String, chatId: String): Boolean
    fun fetchById(messageId: String): Flow<MessageDto>
    fun fetchAvailableToUser(): Flow<List<MessageDto>>
}