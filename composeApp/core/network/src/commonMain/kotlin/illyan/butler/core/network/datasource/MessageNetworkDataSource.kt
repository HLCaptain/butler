package illyan.butler.core.network.datasource

import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface MessageNetworkDataSource {
    fun fetchNewMessages(source: Source.Server): Flow<List<Message>>

    fun fetchByChatId(source: Source.Server, chatId: Uuid): Flow<List<Message>>

    /**
     * Update a message.
     * @return updated message.
     */
    suspend fun upsert(message: Message): Message

    suspend fun create(message: Message): Message

    /**
     * Delete a message.
     * @return true if the message is deleted.
     */
    suspend fun delete(message: Message): Boolean
    fun fetchById(source: Source.Server, messageId: Uuid): Flow<Message>
}
