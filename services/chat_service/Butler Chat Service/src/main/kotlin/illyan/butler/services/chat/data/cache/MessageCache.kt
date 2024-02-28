package illyan.butler.services.chat.data.cache

import illyan.butler.services.chat.data.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessageCache {
    suspend fun getMessage(messageId: String): MessageDto

    /**
     * Returns a flow of messages that affects the user, like new messages for chats the user is in.
     */
    fun getChangedMessagesByUser(userId: String): Flow<List<MessageDto>>
    fun getChangedMessagesByChat(chatId: String): Flow<List<MessageDto>>
    suspend fun setMessage(message: MessageDto): MessageDto
    suspend fun deleteMessage(messageId: String): Boolean
}