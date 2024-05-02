package illyan.butler.services.chat.data.exposed

import illyan.butler.services.chat.data.db.ResourceDatabase
import illyan.butler.services.chat.data.model.chat.ResourceDto
import illyan.butler.services.chat.data.schema.ChatMembers
import illyan.butler.services.chat.data.schema.MessageResources
import illyan.butler.services.chat.data.schema.Messages
import illyan.butler.services.chat.data.schema.Resources
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.core.annotation.Single

@Single
class ResourceExposedDatabase(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher
): ResourceDatabase {
    init {
        transaction(database) {
            SchemaUtils.create(Messages, ChatMembers, Resources, MessageResources)
        }
    }

    override suspend fun createResource(userId: String, messageId: String, resource: ResourceDto): ResourceDto {
        return newSuspendedTransaction(dispatcher, database) {
            // Find chat based on message.chatId
            // Check if the user is in the chat
            // Insert resource.id to Resources table
            // Connect resource.id to message.id in MessageResources table
            val message = Messages.selectAll().where(Messages.id eq messageId).firstOrNull() ?: throw Exception("Cannot attach resource to message, message not found")
            val isUserPartOfChat = ChatMembers.selectAll().where((ChatMembers.memberId eq userId) and (ChatMembers.chatId eq message[Messages.chatId])).count() > 0
            if (!isUserPartOfChat) throw Exception("User is not part of chat")
            val newResourceId = Resources.insertAndGetId {
                it[this.type] = resource.type
                it[this.data] = resource.data
            }
            MessageResources.insert {
                it[this.messageId] = messageId
                it[this.resourceId] = newResourceId
            }
            resource.copy(id = newResourceId.value)
        }
    }

    override suspend fun getResource(userId: String, resourceId: String): ResourceDto {
        return newSuspendedTransaction(dispatcher, database) {
            // Check if user is part of chat where message is which is connected to resource
            if (!canUserAccessResource(resourceId, userId)) {
                throw Exception("User cannot access resource")
            }
            Resources.selectAll().where(Resources.id eq resourceId).first().let {
                ResourceDto(
                    id = it[Resources.id].value,
                    type = it[Resources.type],
                    data = it[Resources.data]
                )
            }
        }
    }

    override suspend fun deleteResource(userId: String, resourceId: String): Boolean {
        return newSuspendedTransaction(dispatcher, database) {
            if (!canUserAccessResource(resourceId, userId)) {
                throw Exception("User cannot access resource")
            }
            MessageResources.deleteWhere { MessageResources.resourceId eq resourceId }
            Resources.deleteWhere { Resources.id eq resourceId } > 0
        }
    }

    private fun canUserAccessResource(resourceId: String, userId: String): Boolean {
        val messageId = MessageResources.selectAll().where(MessageResources.resourceId eq resourceId).firstOrNull()?.get(MessageResources.messageId) ?: throw Exception("Resource not found")
        val message = Messages.selectAll().where(Messages.id eq messageId).firstOrNull() ?: throw Exception("Resource not found")
        val isUserPartOfChat = ChatMembers.selectAll().where((ChatMembers.memberId eq userId) and (ChatMembers.chatId eq message[Messages.chatId])).count() > 0
        return isUserPartOfChat
    }
}
