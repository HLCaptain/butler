package illyan.butler.data.message

import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
interface MessageRepository {
    fun getMessageFlow(messageId: Uuid, source: Source): Flow<Message?>
    fun getChatMessagesFlow(chatId: Uuid, source: Source): Flow<List<Message>>
    fun getMessagesBySource(source: Source): Flow<List<Message>>
    suspend fun create(message: Message): Uuid
    suspend fun upsert(message: Message): Uuid
    suspend fun delete(message: Message)
}
