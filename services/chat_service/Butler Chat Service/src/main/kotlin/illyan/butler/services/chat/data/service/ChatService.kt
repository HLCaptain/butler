package illyan.butler.services.chat.data.service

import illyan.butler.services.chat.data.cache.ChatCache
import illyan.butler.services.chat.data.db.ChatDatabase
import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import illyan.butler.services.chat.data.utils.getLastMonthDate
import illyan.butler.services.chat.data.utils.getLastWeekDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.insertIgnoreAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single
import kotlin.time.Duration.Companion.seconds

@Single
class ChatService(
    private val chatServiceCache: ChatCache,
    private val chatServiceDatabase: ChatDatabase
) {


    // Polling messages and chats is not a good practice but im gonna do it anyway
    val pollingInterval = 1.seconds
    private val _chats = MutableStateFlow(emptyList<ChatDto>())
    val chats = _chats.asStateFlow()
    private val _messages = MutableStateFlow(emptyList<MessageDto>())
    val messages = _messages.asStateFlow()

    init {
        // Poll for new messages
        // Poll for new chats
        newSuspendedTransaction(Dispatchers.IO) {
            while (true) {
                val newChats = Chats.selectAll().map { it.toChatDto() }
                val newMessages = Messages.selectAll().map { it.toMessageDto() }
                _chats.value = newChats
                _messages.value = newMessages
                kotlinx.coroutines.delay(pollingInterval)
            }
        }
    }

    // Returns a state flow of new messages
    fun receiveMessages(userId: String, chatId: String) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chatId)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            Messages.selectAll().where(Messages.chatId eq chatId).map { it.toMessageDto() }
        } else {
            throw Exception("User is not in chat")
        }
    }
    fun receiveMessages(userId: String) = newSuspendedTransaction(Dispatchers.IO) {
        val userChats = ChatMembers.userId eq userId
        val chats = Chats.innerJoin(ChatMembers)
            .selectAll()
            .where { userChats }
        chats.map { it.toChatDto() }
    }
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
        // Insert content urls id to MessageContentUrls table
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
        // Insert content urls id to MessageContentUrls table
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
            } > 0 // Deleted more than 0 rows
        } else {
            throw Exception("User is not in chat")
        }
    }

    fun receiveChats(userId: String) {}
    suspend fun getChat(userId: String, chatId: String) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chatId)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            Chats.selectAll().where(Chats.id eq chatId).first().toChatDto()
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
            Chats.deleteWhere { Chats.id eq chatId } > 0 // Deleted more than 0 rows
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
    ) = newSuspendedTransaction(Dispatchers.IO) {
        val userChats = ChatMembers.userId eq userId
        val chats = Chats.innerJoin(ChatMembers)
            .selectAll()
            .where { userChats and (Messages.time greaterEq fromDate) and (Messages.time lessEq toDate) }
        chats.map { it.toChatDto() }
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
    ) = newSuspendedTransaction(Dispatchers.IO) {
        val userChats = ChatMembers.userId eq userId
        val chats = Chats.innerJoin(ChatMembers)
            .selectAll()
            .where { userChats }
            .sortedBy { Messages.time }
            .drop(offset)
            .take(limit)
        chats.map { it.toChatDto() }
    }

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        timestamp: Long
    ) = newSuspendedTransaction(Dispatchers.IO) {
        val userChats = ChatMembers.userId eq userId
        val chats = Chats.innerJoin(ChatMembers)
            .selectAll()
            .where { userChats and (Messages.time lessEq timestamp) }
            .sortedBy { Messages.time }
            .take(limit)
        chats.map { it.toChatDto() }
    }

    suspend fun getPreviousChats(
        userId: String,
        limit: Int,
        offset: Int
    ) = newSuspendedTransaction(Dispatchers.IO) {
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

    suspend fun getPreviousMessages(
        userId: String,
        chatId: String,
        limit: Int,
        timestamp: Long
    ) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chatId)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            val messages = Messages.selectAll().where { (Messages.time lessEq timestamp) and (Messages.chatId eq chatId) }
                .sortedBy { Messages.time }
                .reversed()
                .take(limit)
            messages.map { it.toMessageDto() }
        } else {
            throw Exception("User is not in chat")
        }
    }

    suspend fun getMessages(
        userId: String,
        chatId: String,
        limit: Int,
        offset: Int
    ) = newSuspendedTransaction(Dispatchers.IO) {
        val userChat = (ChatMembers.userId eq userId) and (ChatMembers.chatId eq chatId)
        val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
        if (isUserInChat) {
            val messages = Messages.selectAll().where(Messages.chatId eq chatId)
                .sortedBy { Messages.time }
                .drop(offset)
                .take(limit)
            messages.map { it.toMessageDto() }
        } else {
            throw Exception("User is not in chat")
        }
    }

    private fun ResultRow.toMessageDto() = MessageDto(
        id = this[Messages.id].value,
        senderId = this[Messages.senderId],
        message = this[Messages.message],
        time = this[Messages.time],
        contentUrls = MessageContentUrls
            .selectAll()
            .where(MessageContentUrls.messageId eq this[Messages.id])
            .map { it[MessageContentUrls.urlId].value }
    )

    private fun ResultRow.toChatDto() = ChatDto(
        id = this[Chats.id].value,
        name = this[Chats.name],
        members = ChatMembers.selectAll().where(ChatMembers.chatId eq this[Chats.id]).map { it[ChatMembers.userId] }
    )
}