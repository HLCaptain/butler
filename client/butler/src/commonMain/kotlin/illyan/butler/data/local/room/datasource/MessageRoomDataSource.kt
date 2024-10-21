package illyan.butler.data.local.room.datasource

import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toRoomModel
import illyan.butler.data.local.room.dao.MessageDao
import illyan.butler.model.DomainMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class MessageRoomDataSource(
    private val messageDao: MessageDao
) : MessageLocalDataSource {
    override suspend fun insertMessage(message: DomainMessage) {
        messageDao.insertMessage(message.toRoomModel())
    }

    override suspend fun insertMessages(messages: List<DomainMessage>) {
        messageDao.insertMessages(messages.map { it.toRoomModel() })
    }

    override suspend fun upsertMessage(message: DomainMessage) {
        messageDao.upsertMessage(message.toRoomModel())
    }

    override suspend fun replaceMessage(oldMessageId: String, newMessage: DomainMessage) {
        messageDao.replaceMessage(oldMessageId, newMessage.toRoomModel())
    }

    override suspend fun deleteMessageById(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }

    override suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    override suspend fun deleteAllMessagesForChat(chatId: String) {
        messageDao.deleteAllChatMessagesForChat(chatId)
    }

    override fun getMessageById(messageId: String): Flow<DomainMessage?> {
        return messageDao.getMessageById(messageId).map { it?.toDomainModel() }
    }

    override suspend fun upsertMessages(newMessages: List<DomainMessage>) {
        messageDao.upsertMessages(newMessages.map { it.toRoomModel() })
    }

    override fun getAccessibleMessagesForUser(userId: String): Flow<List<DomainMessage>> {
        return messageDao.getAccessibleMessagesForUser(userId).map { messages ->
            messages.map { it.toDomainModel() }
        }
    }

    override fun getMessagesByChatId(chatId: String): Flow<List<DomainMessage>> {
        return messageDao.getMessagesByChatId(chatId).map { messages ->
            messages.map { it.toDomainModel() }
        }
    }
}
