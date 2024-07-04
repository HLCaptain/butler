package illyan.butler.data.sqldelight.datasource

import illyan.butler.data.local.datasource.ChatLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.ChatMember
import illyan.butler.domain.model.DomainChat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class ChatSqlDelightDataSource(private val databaseHelper: DatabaseHelper) : ChatLocalDataSource {
    override fun getChat(key: String): Flow<DomainChat?> {
        return databaseHelper.queryAsOneOrNullFlow { it.chatQueries.select(key) }.map { it?.toDomainModel() }
    }

    override suspend fun upsertChat(chat: DomainChat) {
        databaseHelper.withDatabase { it.chatQueries.upsert(chat.toLocalModel()) }
    }

    override suspend fun deleteChatById(chatId: String) {
        databaseHelper.withDatabase { it.chatQueries.delete(chatId) }
    }

    override suspend fun deleteAllChats() {
        databaseHelper.withDatabase { it.chatQueries.deleteAll() }
    }

    override suspend fun upsertChats(chats: List<DomainChat>) {
        databaseHelper.withDatabase { db ->
            chats.forEach { chat ->
                val currentMembers = chat.members.map { ChatMember("${it};${chat.id}", it, chat.id) }
                db.chatMemberQueries.deleteAllChatMembers(chat.id)
                currentMembers.forEach { db.chatMemberQueries.upsert(it) }
                db.chatQueries.upsert(chat.toLocalModel())
            }
        }
    }

    override suspend fun deleteChatByUserId(userId: String) {
        databaseHelper.withDatabase {
            it.chatMemberQueries.selectAllUserChats(userId).executeAsList().forEach { chat ->
                it.chatMemberQueries.deleteAllChatMembers(chat.id)
                it.chatQueries.delete(chat.id)
            }
        }
    }

    override fun getChatsByUser(userId: String): Flow<List<DomainChat>?> {
        return databaseHelper.queryAsListFlow { it.chatQueries.selectAll() }.map { chats ->
            Napier.v { "Chats: ${chats.map { it.id }}" }
            chats // FIXME: all chat is exposed this way, think about security when we have more users on a single device, accessing each other's chats
                .filter { it.members.contains(userId) }
                .map { it.toDomainModel() }
        }
    }
}