package illyan.butler.services.chat.data.cache

import illyan.butler.services.chat.data.model.chat.MessageDto
import kotlinx.coroutines.flow.Flow

interface MessageCache {
    fun getMessage(messageId: String): MessageDto
    fun getMessagesByChat(chatId: String): List<MessageDto>
    fun getMessagesByUser(chatId: String, userId: String): List<MessageDto>

    /**
     * Returns a flow of messages that affects the user, like new messages for chats the user is in.
     */
    fun getChangedMessagesByUser(userId: String): Flow<List<MessageDto>>
    fun getChangedMessagesByChat(chatId: String): Flow<List<MessageDto>>
    fun setMessage(message: MessageDto): MessageDto
    fun deleteMessage(messageId: String)
}