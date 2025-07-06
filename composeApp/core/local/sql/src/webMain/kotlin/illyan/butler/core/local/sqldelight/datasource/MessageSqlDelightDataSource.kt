package illyan.butler.core.local.sqldelight.datasource

import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.local.sqldelight.DatabaseHelper
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import illyan.butler.core.local.sqldelight.mapping.toDomainModel
import illyan.butler.core.local.sqldelight.mapping.toSqlDelightModel
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class MessageSqlDelightDataSource(
    private val databaseHelper: DatabaseHelper<ButlerDatabase>
) : MessageLocalDataSource {

    override suspend fun insertMessage(message: Message) {
        Napier.d { "Inserting message with id: ${message.id}" }
        val sqlDelightMessage = message.toSqlDelightModel()
        databaseHelper.withDatabase {
            messageQueries.insertMessage(
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
    }

    override suspend fun insertMessages(messages: List<Message>) {
        Napier.d { "Inserting ${messages.size} messages" }
        databaseHelper.withDatabase {
            transaction {
                messages.forEach { message ->
                    val sqlDelightMessage = message.toSqlDelightModel()
                    messageQueries.insertMessage(
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
            }
        }
    }

    override suspend fun upsertMessage(message: Message) {
        Napier.d { "Upserting message with id: ${message.id}" }
        val sqlDelightMessage = message.toSqlDelightModel()
        databaseHelper.withDatabase {
            messageQueries.insertOrReplaceMessage(
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
    }

    override suspend fun replaceMessage(oldMessageId: Uuid, newMessage: Message) {
        Napier.d { "Replacing message with id: $oldMessageId with new message with id: ${newMessage.id}" }
        databaseHelper.withDatabase {
            transaction {
                messageQueries.deleteMessage(oldMessageId.toString())
                val sqlDelightMessage = newMessage.toSqlDelightModel()
                messageQueries.insertOrReplaceMessage(
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
        }
    }

    override suspend fun deleteMessageById(messageId: Uuid) {
        Napier.d { "Deleting message with id: $messageId" }
        databaseHelper.withDatabase {
            messageQueries.deleteMessage(messageId.toString())
        }
    }

    override suspend fun deleteAllMessages() {
        Napier.d { "Deleting all messages" }
        databaseHelper.withDatabase {
            messageQueries.deleteAllMessages()
        }
    }

    override suspend fun deleteAllMessagesForChat(chatId: Uuid) {
        Napier.d { "Deleting all messages for chat with id: $chatId" }
        databaseHelper.withDatabase {
            messageQueries.deleteAllMessagesForChat(chatId.toString())
        }
    }

    override fun getMessageById(messageId: Uuid): Flow<Message?> {
        Napier.d { "Getting message with id: $messageId" }
        return databaseHelper.queryAsOneOrNullFlow {
            messageQueries.selectMessageById(messageId.toString())
        }.map { it?.toDomainModel() }
    }

    override fun getMessagesByChatId(chatId: Uuid): Flow<List<Message>> {
        Napier.d { "Getting messages for chat with id: $chatId" }
        return databaseHelper.queryAsListFlow {
            messageQueries.selectMessagesByChatId(chatId.toString())
        }.map { messages -> messages.map { it.toDomainModel() } }
    }

    override fun getMessagesBySource(source: Source): Flow<List<Message>> {
        Napier.d { "Getting messages with source: $source" }
        return databaseHelper.queryAsListFlow {
            messageQueries.selectMessagesBySource(Json.encodeToString(source))
        }.map { messages -> messages.map { it.toDomainModel() } }
    }

    override suspend fun upsertMessages(newMessages: List<Message>) {
        databaseHelper.withDatabase {
            transaction {
                newMessages.forEach { message ->
                    val sqlDelightMessage = message.toSqlDelightModel()
                    messageQueries.insertOrReplaceMessage(
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
            }
        }
    }
}
