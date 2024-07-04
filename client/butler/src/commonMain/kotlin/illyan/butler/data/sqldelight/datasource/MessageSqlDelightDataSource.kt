package illyan.butler.data.sqldelight.datasource

import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.domain.model.DomainMessage
import kotlinx.coroutines.flow.Flow
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

    override suspend fun updateMessage(message: DomainMessage) {
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

    override fun getAllMessagesForChat(chatId: String): Flow<List<DomainMessage>> {
        return databaseHelper.queryAsListFlow {
            it.messageQueries.selectByChat(chatId)
        }.map { messages ->
            messages.map { it.toDomainModel() }
        }
    }
}