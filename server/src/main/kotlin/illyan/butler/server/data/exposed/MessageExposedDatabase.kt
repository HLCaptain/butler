package illyan.butler.server.data.exposed

import illyan.butler.server.data.db.MessageDatabase
import illyan.butler.server.data.schema.Chats
import illyan.butler.server.data.schema.MessageResources
import illyan.butler.server.data.schema.Messages
import illyan.butler.server.data.schema.Resources
import illyan.butler.server.data.service.ApiException
import illyan.butler.shared.model.chat.MessageDto
import illyan.butler.shared.model.response.StatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.batchInsert
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class MessageExposedDatabase(
    private val database: R2dbcDatabase,
    private val dispatcher: CoroutineDispatcher,
    coroutineScopeIO: CoroutineScope
): MessageDatabase {
    init {
        coroutineScopeIO.launch {
            suspendTransaction(db = database) {
                SchemaUtils.create(Messages, Resources, MessageResources)
            }
        }
    }
    override suspend fun sendMessage(userId: String, message: MessageDto): MessageDto {
        return suspendTransaction(dispatcher, db = database) {
            val userUuid = UUID.fromString(userId)
            val userChat = (Chats.ownerId eq userUuid) and (Chats.id eq UUID.fromString(message.chatId))
            val isUserInChat = Chats.selectAll().where(userChat).count() > 0
            val nowMillis = Clock.System.now().toEpochMilliseconds()
            val newMessageId = if (isUserInChat) {
                Messages.insertAndGetId {
                    it[time] = nowMillis
                    it[senderId] = userId
                    it[chatId] = entityId(message.chatId, Chats)
                    it[this.message] = message.message
                }
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
            // Insert content urls if not yet inserted in ContentUrls table
            // Insert content urls id to MessageContentUrls table
            MessageResources.batchInsert(message.resourceIds) {
                this[MessageResources.messageId] = newMessageId
                this[MessageResources.resourceId] = UUID.fromString(it)
            }
            message.copy(id = newMessageId.value.toString(), time = nowMillis)
        }
    }

    override suspend fun editMessage(userId: String, message: MessageDto): MessageDto {
        return suspendTransaction(dispatcher, db = database) {
            val userUuid = UUID.fromString(userId)
            val userChat = (Chats.ownerId eq userUuid) and (Chats.id eq UUID.fromString(message.chatId))
            val isUserInChat = Chats.selectAll().where(userChat).count() > 0
            val messageUuid = UUID.fromString(message.id)
            if (isUserInChat) {
                Messages.update({ Messages.id eq messageUuid }) { it[this.message] = message.message }
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
            // Remove all content urls for the message
            // Insert content urls if not yet inserted in ContentUrls table
            // Insert content urls id to MessageContentUrls table
            val currentMessageResources = MessageResources.selectAll().where(MessageResources.messageId eq messageUuid).toList()
            val removedResources = currentMessageResources.filter { resource ->
                !message.resourceIds.contains(resource[MessageResources.resourceId].value.toString())
            }
            removedResources.forEach { resource ->
                MessageResources.deleteWhere { (messageId eq messageUuid) and (resourceId eq resource[resourceId]) }
            }
            val addedResources = message.resourceIds.filter { id ->
                currentMessageResources.none { it[MessageResources.resourceId].value.toString() == id }
            }
            MessageResources.batchInsert(addedResources) {
                this[MessageResources.messageId] = messageUuid
                this[MessageResources.resourceId] = UUID.fromString(it)
            }
            message
        }
    }

    override suspend fun deleteMessage(userId: String, chatId: String, messageId: String): Boolean {
        return suspendTransaction(dispatcher, db = database) {
            val userUuid = UUID.fromString(userId)
            val userChat = (Chats.ownerId eq userUuid) and (Chats.id eq UUID.fromString(chatId))
            val isUserInChat = Chats.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                // Deleted more than 0 rows
                val messageUuid = UUID.fromString(messageId)
                val chatUuid = UUID.fromString(chatId)
                MessageResources.deleteWhere { MessageResources.messageId eq messageUuid }
                Messages.deleteWhere { (id eq messageUuid) and (Messages.chatId eq chatUuid) and (senderId eq userId) } > 0
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
        }
    }

    override suspend fun getPreviousMessages(userId: String, chatId: String, limit: Int, timestamp: Long): List<MessageDto> {
        return suspendTransaction(dispatcher, db = database) {
            val userUuid = UUID.fromString(userId)
            val chatUuid = UUID.fromString(chatId)
            val userChat = (Chats.ownerId eq userUuid) and (Chats.id eq chatUuid)
            val isUserInChat = Chats.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                val messages = Messages.selectAll()
                    .where { (Messages.time lessEq timestamp) and (Messages.chatId eq chatUuid) }
                    .toList()
                    .sortedBy { Messages.time }
                    .reversed()
                    .take(limit)
                messages.map { it.toMessageDto() }
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
        }
    }

    override suspend fun getMessages(userId: String, chatId: String, limit: Int, offset: Int): List<MessageDto> {
        return suspendTransaction(dispatcher, db = database) {
            val userUuid = UUID.fromString(userId)
            val chatUuid = UUID.fromString(chatId)
            val userChat = (Chats.ownerId eq userUuid) and (Chats.id eq chatUuid)
            val isUserInChat = Chats.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                val messages = Messages.selectAll().where(Messages.chatId eq chatUuid)
                    .toList()
                    .sortedBy { Messages.time }
                    .drop(offset)
                    .take(limit)
                messages.map { it.toMessageDto() }
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
        }
    }

    override suspend fun getMessages(userId: String, chatId: String): List<MessageDto> {
        return suspendTransaction(dispatcher, db = database) {
            val userUuid = UUID.fromString(userId)
            val chatUuid = UUID.fromString(chatId)
            val userChat = (Chats.ownerId eq userUuid) and (Chats.id eq chatUuid)
            val isUserInChat = Chats.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                Messages
                    .selectAll()
                    .where(Messages.chatId eq chatUuid)
                    .toList()
                    .map { it.toMessageDto() }
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
        }
    }

    override fun getChangedMessagesAffectingUser(userId: String): Flow<List<MessageDto>> {
        return flow {
            var previousMessages: Set<MessageDto>? = null
            while (true) {
                val messages = getMessages(userId).toSet()
                val changedMessages = previousMessages?.let {
                    messages.filter { message -> message !in it }
                } ?: messages
                if (changedMessages.isNotEmpty()) emit(changedMessages.toList())
                previousMessages = messages
                delay(10000)
            }
        }
    }

    override suspend fun getMessages(userId: String): List<MessageDto> {
        return suspendTransaction(dispatcher, db = database) {
            // Get all messages related to the user (including messages from chats the user is a member of)
            val userChatIds = Chats.selectAll().where(Chats.ownerId eq UUID.fromString(userId)).map { it[Chats.id] }.toList()
            val messages = Messages.selectAll().where { Messages.chatId inList userChatIds }
            messages.map { it.toMessageDto() }.toList()
        }
    }

    override fun getPreviousMessagesFlow(userId: String, chatId: String, limit: Int, timestamp: Long): Flow<List<MessageDto>> = flow {
        emit(getPreviousMessages(userId, chatId, limit, timestamp))
    }

    override fun getMessagesFlow(userId: String, chatId: String, limit: Int, offset: Int): Flow<List<MessageDto>> = flow {
        emit(getMessages(userId, chatId, limit, offset))
    }

    override fun getMessagesFlow(userId: String, chatId: String): Flow<List<MessageDto>> = flow {
        emit(getMessages(userId, chatId))
    }

    override fun getMessagesFlow(userId: String): Flow<List<MessageDto>> = flow {
        emit(getMessages(userId))
    }

    override fun getChangedMessagesAffectingChat(userId: String, chatId: String): Flow<List<MessageDto>> {
        // This is a simple implementation, a more robust one would listen to database changes.
        return flow {
            var previousMessages: Set<MessageDto>? = null
            while (true) {
                val currentMessages = getMessages(userId, chatId).toSet()
                val changedMessages = previousMessages?.let {
                    currentMessages.filter { message -> message !in it }.toSet()
                } ?: currentMessages
                if (changedMessages.isNotEmpty()) {
                    emit(changedMessages.toList())
                }
                previousMessages = currentMessages
                delay(5000) // Poll every 5 seconds
            }
        }
    }
}

suspend fun ResultRow.toMessageDto() = MessageDto(
    id = this[Messages.id].value.toString(),
    senderId = this[Messages.senderId],
    message = this[Messages.message],
    time = this[Messages.time],
    chatId = this[Messages.chatId].value.toString(),
    resourceIds = MessageResources
        .selectAll()
        .where(MessageResources.messageId eq this[Messages.id])
        .map { it[MessageResources.resourceId].value.toString() }
        .toList()
)
