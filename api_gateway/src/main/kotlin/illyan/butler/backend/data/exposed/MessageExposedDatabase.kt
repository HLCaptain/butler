package illyan.butler.backend.data.exposed

import illyan.butler.backend.data.db.MessageDatabase
import illyan.butler.backend.data.model.chat.MessageDto
import illyan.butler.backend.data.schema.ChatMembers
import illyan.butler.backend.data.schema.Chats
import illyan.butler.backend.data.schema.MessageResources
import illyan.butler.backend.data.schema.Messages
import illyan.butler.backend.data.schema.Resources
import illyan.butler.backend.data.service.ApiException
import illyan.butler.backend.endpoints.utils.StatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class MessageExposedDatabase(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher
): MessageDatabase {
    init {
        transaction(database) {
            SchemaUtils.create(Messages, ChatMembers, Resources, MessageResources)
        }
    }
    override suspend fun sendMessage(userId: String, message: MessageDto): MessageDto {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.memberId eq userId) and (ChatMembers.chatId eq message.chatId)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            val nowMillis = Clock.System.now().toEpochMilliseconds()
            val newMessageId = if (isUserInChat) {
                Messages.insertAndGetId {
                    it[time] = nowMillis
                    it[senderId] = userId
                    it[chatId] = entityId(message.chatId!!, Chats)
                    it[this.message] = message.message
                }
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
            // Insert content urls if not yet inserted in ContentUrls table
            // Insert content urls id to MessageContentUrls table
            message.resourceIds.forEach { id ->
                val resource = Resources.selectAll().where(Resources.id eq id).first()
                MessageResources.insertIgnore {
                    it[messageId] = newMessageId
                    it[resourceId] = resource[Resources.id]
                }
            }
            message.copy(id = newMessageId.value, time = nowMillis)
        }
    }

    override suspend fun editMessage(userId: String, message: MessageDto): MessageDto {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.memberId eq userId) and (ChatMembers.chatId eq message.chatId)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                Messages.update({ Messages.id eq message.id }) { it[this.message] = message.message }
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
            // Remove all content urls for the message
            // Insert content urls if not yet inserted in ContentUrls table
            // Insert content urls id to MessageContentUrls table
            val currentMessageResources = MessageResources.selectAll().where(MessageResources.messageId eq message.id!!)
            val removedResources = currentMessageResources.filter { resource ->
                !message.resourceIds.contains(resource[MessageResources.resourceId].value)
            }
            removedResources.forEach { resource ->
                MessageResources.deleteWhere { (messageId eq message.id) and (resourceId eq resource[resourceId]) }
            }
            val addedResources = message.resourceIds.filter { id ->
                currentMessageResources.none { it[MessageResources.resourceId].value == id }
            }
            addedResources.forEach { id ->
                val resource = Resources.selectAll().where(Resources.id eq id).first()
                MessageResources.insertIgnore {
                    it[messageId] = message.id
                    it[resourceId] = resource[Resources.id]
                }
            }
            message
        }
    }

    override suspend fun deleteMessage(userId: String, chatId: String, messageId: String): Boolean {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.memberId eq userId) and (ChatMembers.chatId eq chatId)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                // Deleted more than 0 rows
                MessageResources.deleteWhere { MessageResources.messageId eq messageId }
                Messages.deleteWhere { (id eq messageId) and (Messages.chatId eq chatId) and (senderId eq userId) } > 0
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
        }
    }

    override suspend fun getPreviousMessages(userId: String, chatId: String, limit: Int, timestamp: Long): List<MessageDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.memberId eq userId) and (ChatMembers.chatId eq chatId)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                val messages = Messages.selectAll().where { (Messages.time lessEq timestamp) and (Messages.chatId eq chatId) }
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
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.memberId eq userId) and (ChatMembers.chatId eq chatId)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                val messages = Messages.selectAll().where(Messages.chatId eq chatId)
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
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.memberId eq userId) and (ChatMembers.chatId eq chatId)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                Messages
                    .selectAll()
                    .where(Messages.chatId eq chatId)
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
        return newSuspendedTransaction(dispatcher, database) {
            // Get all messages related to the user (including messages from chats the user is a member of)
            val userChats = ChatMembers.selectAll().where(ChatMembers.memberId eq userId)
            val messages = Messages.selectAll().where { Messages.chatId inList userChats.map { it[ChatMembers.chatId] } }
            messages.map { it.toMessageDto() }
        }
    }

    override fun getChangedMessagesAffectingChat(userId: String, chatId: String): Flow<List<MessageDto>> {
        return flow {
            var previousMessages: Set<MessageDto>? = null
            while (true) {
                val messages = getMessages(userId, chatId).toSet()
                val changedMessages = previousMessages?.let {
                    messages.filter { message -> message !in it }
                } ?: emptySet()
                if (changedMessages.isNotEmpty()) emit(changedMessages.toList())
                previousMessages = messages
                delay(1000)
            }
        }
    }
}

fun ResultRow.toMessageDto() = MessageDto(
    id = this[Messages.id].value,
    senderId = this[Messages.senderId],
    message = this[Messages.message],
    time = this[Messages.time],
    chatId = this[Messages.chatId].value,
    resourceIds = MessageResources
        .selectAll()
        .where(MessageResources.messageId eq this[Messages.id])
        .map { it[MessageResources.resourceId].value }
)
