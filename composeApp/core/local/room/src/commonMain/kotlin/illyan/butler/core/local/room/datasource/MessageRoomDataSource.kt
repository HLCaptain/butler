package illyan.butler.core.local.room.datasource

import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.local.room.dao.MessageDao
import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class MessageRoomDataSource(
    private val messageDao: MessageDao
) : MessageLocalDataSource {
    override suspend fun insertMessage(message: Message) {
        messageDao.insertMessage(message.toRoomModel())
    }

    override suspend fun insertMessages(messages: List<Message>) {
        messageDao.insertMessages(messages.map { it.toRoomModel() })
    }

    override suspend fun upsertMessage(message: Message) {
        messageDao.upsertMessage(message.toRoomModel())
    }

    override suspend fun replaceMessage(oldMessageId: Uuid, newMessage: Message) {
        messageDao.replaceMessage(oldMessageId.toString(), newMessage.toRoomModel())
    }

    override suspend fun deleteMessageById(messageId: Uuid) {
        messageDao.deleteMessageById(messageId.toString())
    }

    override suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    override suspend fun deleteAllMessagesForChat(chatId: Uuid) {
        messageDao.deleteAllChatMessagesForChat(chatId.toString())
    }

    override fun getMessageById(messageId: Uuid): Flow<Message?> {
        return messageDao.getMessageById(messageId.toString()).map { it?.toDomainModel() }
    }

    override suspend fun upsertMessages(newMessages: List<Message>) {
        messageDao.upsertMessages(newMessages.map { it.toRoomModel() })
    }

    override fun getMessagesBySource(source: Source): Flow<List<Message>> {
        return messageDao.getMessagesBySource(source).map { messages ->
            messages.map { it.toDomainModel() }
        }
    }

    override fun getMessagesByChatId(chatId: Uuid): Flow<List<Message>> {
        return messageDao.getMessagesByChatId(chatId.toString()).map { messages ->
            messages.map { it.toDomainModel() }
        }
    }
}
