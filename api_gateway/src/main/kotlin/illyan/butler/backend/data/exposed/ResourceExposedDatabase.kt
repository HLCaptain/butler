package illyan.butler.backend.data.exposed

import illyan.butler.backend.data.db.ResourceDatabase
import illyan.butler.backend.data.model.chat.ResourceDto
import illyan.butler.backend.data.schema.ChatMembers
import illyan.butler.backend.data.schema.MessageResources
import illyan.butler.backend.data.schema.Messages
import illyan.butler.backend.data.schema.Resources
import illyan.butler.backend.data.service.ApiException
import illyan.butler.backend.endpoints.utils.StatusCode
import kotlinx.coroutines.CoroutineDispatcher
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
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

    override suspend fun createResource(userId: String, resource: ResourceDto): ResourceDto {
        return newSuspendedTransaction(dispatcher, database) {
            val newResourceId = Resources.insertAndGetId {
                it[this.type] = resource.type
                it[this.data] = resource.data
            }
            resource.copy(id = newResourceId.value)
        }
    }

    override suspend fun getResource(userId: String, resourceId: String): ResourceDto {
        return newSuspendedTransaction(dispatcher, database) {
            // Check if user is part of chat where message is which is connected to resource
            if (!canUserAccessResource(resourceId, userId)) {
                throw ApiException(StatusCode.ResourceNotFound)
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
                throw ApiException(StatusCode.ResourceNotFound)
            }
            MessageResources.deleteWhere { MessageResources.resourceId eq resourceId }
            Resources.deleteWhere { Resources.id eq resourceId } > 0
        }
    }

    override suspend fun getResources(userId: String): List<ResourceDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChats = ChatMembers.selectAll().where(ChatMembers.memberId eq userId).map { it[ChatMembers.chatId] }
            val userRelatedMessages = Messages.selectAll().where(Messages.chatId inList userChats).map { it[Messages.id] }
            val userRelatedResources = MessageResources.selectAll().where(MessageResources.messageId inList userRelatedMessages).map { it[MessageResources.resourceId] }
            Resources.selectAll().where(Resources.id inList userRelatedResources).map {
                ResourceDto(
                    id = it[Resources.id].value,
                    type = it[Resources.type],
                    data = it[Resources.data]
                )
            }
        }
    }

    private fun canUserAccessResource(resourceId: String, userId: String): Boolean {
        val messageId = MessageResources.selectAll().where(MessageResources.resourceId eq resourceId).firstOrNull()?.get(
            MessageResources.messageId) ?: throw ApiException(StatusCode.ResourceNotFound)
        val message = Messages.selectAll().where(Messages.id eq messageId).firstOrNull() ?: throw Exception("Resource not found")
        val isUserPartOfChat = ChatMembers.selectAll().where((ChatMembers.memberId eq userId) and (ChatMembers.chatId eq message[Messages.chatId])).count() > 0
        return isUserPartOfChat
    }
}
