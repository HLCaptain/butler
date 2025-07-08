package illyan.butler.core.local.sqldelight.datasource

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.sqldelight.DatabaseHelper
import illyan.butler.core.local.sqldelight.db.ButlerDatabase
import illyan.butler.core.local.sqldelight.mapping.toDomainModel
import illyan.butler.core.local.sqldelight.mapping.toSqlDelightModel
import illyan.butler.domain.model.Chat
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
class ChatSqlDelightDataSource(
    private val databaseHelper: DatabaseHelper<ButlerDatabase>
) : ChatLocalDataSource {

    override fun getChat(chatId: Uuid): Flow<Chat?> {
        Napier.d { "Getting chat with id: $chatId" }
        return databaseHelper.queryAsOneFlow {
            chatQueries.selectChatById(chatId.toString())
        }.map { it.toDomainModel() }
    }

    override fun getChatsBySource(source: Source): Flow<List<Chat>> {
        Napier.d { "Getting chats with source: $source" }
        return databaseHelper.queryAsListFlow {
            chatQueries.selectChatsBySource(Json.encodeToString(source))
        }.map { chats ->
            chats.map { it.toDomainModel() }
        }
    }

    override suspend fun upsertChat(chat: Chat) {
        Napier.d { "Upserting chat with id: ${chat.id}" }
        val sqlDelightChat = chat.toSqlDelightModel()
        databaseHelper.withDatabase {
            chatQueries.insertOrReplaceChat(
                id = sqlDelightChat.id,
                createdAt = sqlDelightChat.createdAt,
                source = sqlDelightChat.source,
                title = sqlDelightChat.title,
                summary = sqlDelightChat.summary,
                lastUpdated = sqlDelightChat.lastUpdated,
                models = sqlDelightChat.models
            )
        }
    }

    override suspend fun replaceChat(oldChatId: Uuid, newChat: Chat) {
        Napier.d { "Replacing chat with id: $oldChatId with new chat with id: ${newChat.id}" }
        databaseHelper.withDatabase {
            transaction {
                chatQueries.deleteChat(oldChatId.toString())
                upsertChat(newChat)
            }
        }
    }

    override suspend fun deleteChatById(chatId: Uuid) {
        Napier.d { "Deleting chat with id: $chatId" }
        databaseHelper.withDatabase { chatQueries.deleteChat(chatId.toString()) }
    }

    override suspend fun deleteAllChats() {
        Napier.d { "Deleting all chats" }
        databaseHelper.withDatabase {
            chatQueries.deleteAllChats()
        }
    }

    override suspend fun upsertChats(chats: List<Chat>) {
        Napier.d { "Upserting ${chats.size} chats" }
        databaseHelper.withDatabase {
            chats.forEach { chat ->
                val sqlDelightChat = chat.toSqlDelightModel()
                chatQueries.insertOrReplaceChat(
                    id = sqlDelightChat.id,
                    createdAt = sqlDelightChat.createdAt,
                    source = sqlDelightChat.source,
                    title = sqlDelightChat.title,
                    summary = sqlDelightChat.summary,
                    lastUpdated = sqlDelightChat.lastUpdated,
                    models = sqlDelightChat.models
                )
            }
        }
    }
}
