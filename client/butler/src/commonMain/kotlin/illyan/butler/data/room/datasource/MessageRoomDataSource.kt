package illyan.butler.data.room.datasource

import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toRoomModel
import illyan.butler.data.room.dao.MessageDao
import illyan.butler.domain.model.DomainMessage
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

    override suspend fun updateMessage(message: DomainMessage) {
        messageDao.upsertMessage(message.toRoomModel())
    }

    override suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessageById(messageId)
    }

    override suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    override suspend fun deleteAllMessagesForChat(chatId: String) {
        messageDao.deleteAllChatMessagesForChat(chatId)
    }

    override fun getAllMessagesForChat(chatId: String): Flow<List<DomainMessage>> {
        return messageDao.getMessagesByChatId(chatId).map { messages ->
            messages.map { it.toDomainModel() }
        }
    }
}
