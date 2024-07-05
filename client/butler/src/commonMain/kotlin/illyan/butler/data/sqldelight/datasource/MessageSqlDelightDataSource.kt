package illyan.butler.data.sqldelight.datasource

import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.domain.model.DomainMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class MessageSqlDelightDataSource(private val databaseHelper: DatabaseHelper) : MessageLocalDataSource {
    override suspend fun insertMessage(message: DomainMessage) {
        databaseHelper.withDatabase {
            it.messageQueries.upsert(message.toLocalModel())
        }
    }

    override suspend fun insertMessages(messages: List<DomainMessage>) {
        databaseHelper.withDatabase { db ->
            messages.forEach { db.messageQueries.upsert(it.toLocalModel()) }
        }
    }

    override suspend fun upsertMessage(message: DomainMessage) {
        insertMessage(message) // Since upsert is used for both insert and update
    }

    override suspend fun deleteMessage(messageId: String) {
        databaseHelper.withDatabase {
            it.messageQueries.delete(messageId)
        }
    }

    override suspend fun deleteAllMessages() {
        databaseHelper.withDatabase {
            it.messageQueries.deleteAll()
        }
    }

    override suspend fun deleteAllMessagesForChat(chatId: String) {
        databaseHelper.withDatabase {
            it.messageQueries.deleteAllChatMessagesForChat(chatId)
        }
    }

    override fun getMessageById(messageId: String): Flow<DomainMessage?> {
        return databaseHelper.queryAsOneFlow {
            it.messageQueries.select(messageId)
        }.map { it.toDomainModel() }
    }

    override fun getMessagesByChatId(chatId: String): Flow<List<DomainMessage>> {
        return databaseHelper.queryAsListFlow {
            it.messageQueries.selectByChat(chatId)
        }.map { messages ->
            messages.map { it.toDomainModel() }
        }
    }

    override suspend fun upsertMessages(newMessages: List<DomainMessage>) {
        insertMessages(newMessages)
    }

    override fun getAccessibleMessagesForUser(userId: String): Flow<List<DomainMessage>> {
        return flow {
            while (true) {
                val newMessages = databaseHelper.withDatabase { database ->
//                        Napier.d("Reading chat at $key")
                    // TODO: could use ChatMembership table to get all chats for user, but this is simpler and probably faster
                    val chats = database.chatQueries.selectAll().executeAsList()
                    val userChats = chats.filter { it.members.contains(userId) }
//                        Napier.v { "Chats the user is member of: ${userChats.size} out of ${chats.size}" }
                    val messages = userChats.map { chat ->
                        database.messageQueries.selectByChat(chat.id).executeAsList()
                    }.flatten()
                    messages
                }.map { it.toDomainModel() }
                emit(newMessages)
                delay(1000)
            }
        }
    }
}