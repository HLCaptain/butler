package illyan.butler.services.identity.data.exposed

import illyan.butler.services.identity.data.db.ChatDatabase
import illyan.butler.services.identity.data.model.chat.ChatDto
import illyan.butler.services.identity.data.schema.ChatMembers
import illyan.butler.services.identity.data.schema.Chats
import illyan.butler.services.identity.data.schema.Messages
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class ChatExposedDatabase(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher
) : ChatDatabase {
    override suspend fun getChat(userId: String, chatId: String): ChatDto {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chatId)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                Chats.selectAll().where(Chats.id eq chatId).first().toChatDto()
            } else {
                throw Exception("User is not in chat")
            }
        }
    }

    override suspend fun createChat(userId: String, chat: ChatDto): ChatDto {
        return newSuspendedTransaction(dispatcher, database) {
            val chatId = Chats.insertAndGetId { it[name] = chat.name }
            (chat.members + userId).distinct().forEach { member ->
                ChatMembers.insertIgnore {
                    it[ChatMembers.chatId] = chatId
                    it[ChatMembers.userId] = member
                }
            }
            chat
        }
    }

    override suspend fun editChat(userId: String, chat: ChatDto) {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chat.id!!)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                Chats.update({ Chats.id eq chat.id }) { it[name] = chat.name }
                val currentMembers =
                    ChatMembers.selectAll().where(ChatMembers.chatId eq chat.id).map { it[ChatMembers.userId] }
                val newMembers = chat.members
                val removedMembers = currentMembers.filter { !newMembers.contains(it) }
                val addedMembers = newMembers.filter { !currentMembers.contains(it) }
                removedMembers.forEach { member ->
                    ChatMembers.deleteWhere { (chatId eq chat.id) and (ChatMembers.userId eq member) }
                }
                addedMembers.forEach { member ->
                    ChatMembers.insertIgnore {
                        it[chatId] = chat.id
                        it[ChatMembers.userId] = member
                    }
                }
            } else {
                throw Exception("User is not in chat")
            }
        }
    }

    override suspend fun deleteChat(userId: String, chatId: String): Boolean {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chatId)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                ChatMembers.deleteWhere { ChatMembers.chatId eq chatId }
                Chats.deleteWhere { Chats.id eq chatId } > 0 // Deleted more than 0 rows
            } else {
                throw Exception("User is not in chat")
            }
        }
    }

    override suspend fun getChats(userId: String, fromDate: Long, toDate: Long): List<ChatDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChats = ChatMembers.userId eq userId
            val chats = Chats.innerJoin(ChatMembers)
                .selectAll()
                .where { userChats and (Messages.time greaterEq fromDate) and (Messages.time lessEq toDate) }
            chats.map { it.toChatDto() }
        }
    }

    override suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChats = ChatMembers.userId eq userId
            val chats = Chats.innerJoin(ChatMembers)
                .selectAll()
                .where { userChats }
                .sortedBy { Messages.time }
                .drop(offset)
                .take(limit)
            chats.map { it.toChatDto() }
        }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChats = ChatMembers.userId eq userId
            val chats = Chats.innerJoin(ChatMembers)
                .selectAll()
                .where { userChats and (Messages.time lessEq timestamp) }
                .sortedBy { Messages.time }
                .take(limit)
            chats.map { it.toChatDto() }
        }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChats = ChatMembers.userId eq userId
            val chats = Chats.innerJoin(ChatMembers)
                .selectAll()
                .where { userChats }
                .sortedBy { Messages.time }
                .reversed()
                .drop(offset)
                .take(limit)
            chats.map { it.toChatDto() }
        }
    }



    private fun ResultRow.toChatDto() = ChatDto(
        id = this[Chats.id].value,
        name = this[Chats.name],
        members = ChatMembers.selectAll().where(ChatMembers.chatId eq this[Chats.id]).map { it[ChatMembers.userId] }
    )
}