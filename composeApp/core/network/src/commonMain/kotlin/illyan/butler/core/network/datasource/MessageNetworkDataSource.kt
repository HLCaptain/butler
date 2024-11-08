package illyan.butler.core.network.datasource

import illyan.butler.domain.model.DomainMessage
import kotlinx.coroutines.flow.Flow

interface MessageNetworkDataSource {
    fun fetchNewMessages(): Flow<List<DomainMessage>>

    fun fetchByChatId(chatId: String): Flow<List<DomainMessage>>

    /**
     * Update a message.
     * @return updated message.
     */
    suspend fun upsert(message: DomainMessage): DomainMessage

    /**
     * Delete a message.
     * @return true if the message is deleted.
     */
    suspend fun delete(messageId: String, chatId: String): Boolean
    fun fetchById(messageId: String): Flow<DomainMessage>
    fun fetchAvailableToUser(): Flow<List<DomainMessage>>
}