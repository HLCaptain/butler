package illyan.butler.core.local.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import illyan.butler.core.local.sqldelight.mapping.toDomainModel
import illyan.butler.core.local.sqldelight.mapping.toSqlDelightModel
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class MessageSqlDelightDataSource(
    private val database: ButlerDatabase
) : MessageLocalDataSource {

    override suspend fun insertMessage(message: Message) {
        Napier.d { "Inserting message with id: ${message.id}" }
        val sqlDelightMessage = message.toSqlDelightModel()
        database.messageQueries.insertMessage(
            id = sqlDelightMessage.id,
            createdAt = sqlDelightMessage.createdAt,
            source = sqlDelightMessage.source,
            chatId = sqlDelightMessage.chatId,
            sender = sqlDelightMessage.sender,
            content = sqlDelightMessage.content,
            resourceIds = sqlDelightMessage.resourceIds,
            status = sqlDelightMessage.status
        )
    }

    override suspend fun insertMessages(messages: List<Message>) {
        Napier.d { "Inserting ${messages.size} messages" }
        database.transaction {
            messages.forEach { message ->
                insertMessage(message)
            }
        }
    }

    override suspend fun upsertMessage(message: Message) {
        Napier.d { "Upserting message with id: ${message.id}" }
        val sqlDelightMessage = message.toSqlDelightModel()
        database.messageQueries.insertOrReplaceMessage(
            id = sqlDelightMessage.id,
            createdAt = sqlDelightMessage.createdAt,
            source = sqlDelightMessage.source,
            chatId = sqlDelightMessage.chatId,
            sender = sqlDelightMessage.sender,
            content = sqlDelightMessage.content,
            resourceIds = sqlDelightMessage.resourceIds,
            status = sqlDelightMessage.status
        )
    }

    override suspend fun replaceMessage(oldMessageId: Uuid, newMessage: Message) {
        Napier.d { "Replacing message with id: $oldMessageId with new message with id: ${newMessage.id}" }
        database.transaction {
            database.messageQueries.deleteMessage(oldMessageId.toString())
            upsertMessage(newMessage)
        }
    }

    override suspend fun deleteMessageById(messageId: Uuid) {
        Napier.d { "Deleting message with id: $messageId" }
        database.messageQueries.deleteMessage(messageId.toString())
    }

    override suspend fun deleteAllMessages() {
        Napier.d { "Deleting all messages" }
        database.messageQueries.deleteAllMessages()
    }

    override suspend fun deleteAllMessagesForChat(chatId: Uuid) {
        Napier.d { "Deleting all messages for chat with id: $chatId" }
        database.messageQueries.deleteAllMessagesForChat(chatId.toString())
    }

    override fun getMessageById(messageId: Uuid): Flow<Message?> {
        Napier.d { "Getting message with id: $messageId" }
        return database.messageQueries.selectMessageById(messageId.toString())
            .asFlow()
            .mapToOneOrNull(Dispatchers.Default)
            .map { it?.toDomainModel() }
    }

    override fun getMessagesByChatId(chatId: Uuid): Flow<List<Message>> {
        Napier.d { "Getting messages for chat with id: $chatId" }
        return database.messageQueries.selectMessagesByChatId(chatId.toString())
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { messages -> messages.map { it.toDomainModel() } }
    }

    override fun getMessagesBySource(source: Source): Flow<List<Message>> {
        Napier.d { "Getting messages with source: $source" }
        return database.messageQueries.selectMessagesBySource(Json.encodeToString(source))
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { messages -> messages.map { it.toDomainModel() } }
    }

    override suspend fun upsertMessages(newMessages: List<Message>) {
        database.transaction {
            newMessages.forEach { message ->
                upsertMessage(message)
            }
        }
    }
}
