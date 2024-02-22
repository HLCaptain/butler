package illyan.butler.services.chat.data.service

import com.aventrix.jnanoid.jnanoid.NanoIdUtils
import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import illyan.butler.services.chat.data.utils.NanoIdTable
import illyan.butler.services.chat.data.utils.getLastMonthDate
import illyan.butler.services.chat.data.utils.getLastWeekDate
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.rowNumber
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class ChatService {
    object Chats : NanoIdTable() {
        val name = text("name").nullable()
    }

    object ChatMembers : Table() {
        val chatId = entityId("chatId", Chats)
        val userId = varchar("userId", NanoIdUtils.DEFAULT_SIZE)
        override val primaryKey = PrimaryKey(chatId, userId)
    }

    object Messages : NanoIdTable() {
        val senderId = varchar("senderId", NanoIdUtils.DEFAULT_SIZE)
        val message = text("message").nullable()
        val time = long("time")
        val chatId = entityId("chat", Chats)
    }

    object ContentUrls : NanoIdTable() {
        val url = text("url").uniqueIndex()
    }

    object MessageContentUrls : Table() {
        val messageId = entityId("message", Messages)
        val urlId = entityId("url", ContentUrls)
        override val primaryKey = PrimaryKey(messageId, urlId)
    }

    fun receiveMessages(userId: String, chatId: String) {}
    suspend fun sendMessage(userId: String, message: MessageDto) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq message.chatId!!)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            Messages.insert {
                it[time] = Clock.System.now().toEpochMilliseconds()
                it[senderId] = userId
                it[chatId] = message.chatId
                it[this.message] = message.message
            }
        } else {
            throw Exception("User is not in chat")
        }
        // Insert content urls if not yet inserted in ContentUrls table
        // Insert content urls' id to MessageContentUrls table
        message.contentUrls.forEach { url ->
            var newUrlId = ContentUrls.insertIgnoreAndGetId { it[this.url] = url }
            // InsertIgnoreAndGetId returns null if the row already exists
            if (newUrlId == null) {
                // If the row already exists, get the id of the existing row
                newUrlId = ContentUrls.selectAll().where(ContentUrls.url eq url).first()[ContentUrls.id]
            }
            MessageContentUrls.insertIgnore {
                it[messageId] = message.id!!
                it[urlId] = newUrlId
            }
        }
    }

    suspend fun editMessage(
        userId: String,
        message: MessageDto
    ) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq message.chatId!!)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            Messages.update { it[this.message] = message.message }
        } else {
            throw Exception("User is not in chat")
        }
        // Remove all content urls for the message
        // Insert content urls if not yet inserted in ContentUrls table
        // Insert content urls' id to MessageContentUrls table
        val currentMessageUrls = MessageContentUrls.selectAll().where(MessageContentUrls.messageId eq message.id!!)
        val newMessageUrls = message.contentUrls
        val removedMessageUrls = currentMessageUrls.filter { url ->
            newMessageUrls.contains(url[MessageContentUrls.urlId].value)
        }
        removedMessageUrls.forEach { url ->
            MessageContentUrls.deleteWhere { (messageId eq message.id) and (urlId eq url[urlId]) }
        }
        val addedMessageUrls = message.contentUrls.filter { url ->
            currentMessageUrls.none { it[MessageContentUrls.urlId].value == url }
        }
        addedMessageUrls.forEach { contentUrl ->
            ContentUrls.insertIgnore { it[url] = contentUrl }
            MessageContentUrls.insertIgnore {
                it[messageId] = message.id
                it[urlId] = contentUrl
            }
        }
    }

    suspend fun deleteMessage(
        userId: String,
        chatId: String,
        messageId: String
    ) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chatId)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            Messages.deleteWhere {
                (id eq messageId) and (Messages.chatId eq chatId) and (senderId eq userId)
            }
        } else {
            throw Exception("User is not in chat")
        }
    }

    fun receiveChats(userId: String) {}
    suspend fun getChat(userId: String, chatId: String) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chatId)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            Chats.selectAll().where(Chats.id eq chatId).firstOrNull()
        } else {
            throw Exception("User is not in chat")
        }
    }
    suspend fun createChat(userId: String, chat: ChatDto) = newSuspendedTransaction(Dispatchers.IO) {
        val chatId = Chats.insertAndGetId { it[name] = chat.name }
        (chat.members + userId).distinct().forEach { member ->
            ChatMembers.insertIgnore {
                it[ChatMembers.chatId] = chatId
                it[ChatMembers.userId] = member
            }
        }
        chat
    }
    suspend fun editChat(userId: String, chat: ChatDto) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chat.id!!)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            Chats.update({ Chats.id eq chat.id }) { it[name] = chat.name }
            val currentMembers = ChatMembers.selectAll().where(ChatMembers.chatId eq chat.id).map { it[ChatMembers.userId] }
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
    suspend fun deleteChat(userId: String, chatId: String) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chatId)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            ChatMembers.deleteWhere { ChatMembers.chatId eq chatId }
            Chats.deleteWhere { Chats.id eq chatId }
        } else {
            throw Exception("User is not in chat")
        }
    }

    /**
     * @param fromDate epoch milli
     * @param toDate epoch milli
     * @return list of [ChatDto]
     */
    private suspend fun getChats(
        userId: String,
        fromDate: Long,
        toDate: Long = Clock.System.now().toEpochMilliseconds()
    ) {
    }

    suspend fun getChatsLastMonth(userId: String) = getChats(
        userId = userId,
        fromDate = getLastMonthDate().toEpochMilliseconds()
    )

    suspend fun getChatsLastWeek(userId: String) = getChats(
        userId = userId,
        fromDate = getLastWeekDate().toEpochMilliseconds()
    )

    suspend fun getChats(
        userId: String,
        limit: Int,
        offset: Int
    ) {
    }

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        timestamp: Long
    ) {
    }

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        offset: Int
    ) {
    }

    suspend fun getPreviousMessages(
        userId: String,
        chatId: String,
        limit: Int,
        timestamp: Long
    ) {
    }

    suspend fun getMessages(
        userId: String,
        chatId: String,
        limit: Int,
        offset: Int
    ) {
    }
}