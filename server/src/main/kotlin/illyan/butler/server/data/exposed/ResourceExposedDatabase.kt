package illyan.butler.server.data.exposed

import illyan.butler.server.data.db.ResourceDatabase
import illyan.butler.server.data.schema.Chats
import illyan.butler.server.data.schema.MessageResources
import illyan.butler.server.data.schema.Messages
import illyan.butler.server.data.schema.Resources
import illyan.butler.server.data.service.ApiException
import illyan.butler.shared.model.chat.ResourceDto
import illyan.butler.shared.model.response.StatusCode
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEmpty
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.andWhere
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class ResourceExposedDatabase(
    private val database: R2dbcDatabase,
    private val dispatcher: CoroutineDispatcher,
    coroutineScopeIO: CoroutineScope
): ResourceDatabase {
    init {
        coroutineScopeIO.launch {
            suspendTransaction(db = database) {
                SchemaUtils.create(Messages, Resources, MessageResources)
            }
        }
    }

    override suspend fun createResource(userId: String, resource: ResourceDto): ResourceDto {
        return suspendTransaction(dispatcher, db = database) {
            val newResourceId = Resources.insertAndGetId {
                it[this.type] = resource.type
                it[this.data] = resource.data
            }
            resource.copy(id = newResourceId.value.toString())
        }
    }

    override suspend fun getResource(userId: String, resourceId: String): ResourceDto {
        return suspendTransaction(dispatcher, db = database) {
            // Check if user is part of chat where message is which is connected to resource
            if (!canUserAccessResource(resourceId, userId)) {
                throw ApiException(StatusCode.ResourceNotFound)
            }
            Resources.selectAll().where(Resources.id eq UUID.fromString(resourceId)).first().let {
                ResourceDto(
                    id = it[Resources.id].value.toString(),
                    type = it[Resources.type],
                    data = it[Resources.data]
                )
            }
        }
    }

    override suspend fun deleteResource(userId: String, resourceId: String): Boolean {
        return suspendTransaction(dispatcher, db = database) {
            if (!canUserAccessResource(resourceId, userId)) {
                throw ApiException(StatusCode.ResourceNotFound)
            }
            MessageResources.deleteWhere { MessageResources.resourceId eq UUID.fromString(resourceId) }
            Resources.deleteWhere { id eq UUID.fromString(resourceId) } > 0
        }
    }

    override suspend fun getResources(userId: String): List<ResourceDto> {
        return suspendTransaction(dispatcher, db = database) {
            val userChats = Chats.select(Chats.id, Chats.ownerId).where(Chats.ownerId eq UUID.fromString(userId)).map { it[Chats.id] }.toList()
            val userRelatedMessages = Messages.select(Messages.id, Messages.chatId).where { Messages.chatId inList userChats }.map { it[Messages.id] }.toList()
            val userRelatedResources = MessageResources.selectAll().where { MessageResources.messageId inList userRelatedMessages }.map { it[MessageResources.resourceId] }.toList()
            Resources.selectAll().where { Resources.id inList userRelatedResources }.map {
                ResourceDto(
                    id = it[Resources.id].value.toString(),
                    type = it[Resources.type],
                    data = it[Resources.data]
                )
            }.toList()
        }
    }

    private suspend fun canUserAccessResource(resourceId: String, userId: String): Boolean {
        val referencingMessagesIds = MessageResources.selectAll()
            .where(MessageResources.resourceId eq UUID.fromString(resourceId))
            .map { it[MessageResources.messageId] }
            .onEmpty {
                throw ApiException(StatusCode.ResourceNotFound)
            }.toList()
        val referencingChats = Messages.selectAll()
            .where { Messages.id inList referencingMessagesIds }
            .onEmpty { throw Exception("Resource not found") }
            .map { it[Messages.chatId] }
            .toList()
            .distinct()
        val isUserPartOfReferencingChat = Chats.selectAll()
            .where { Chats.id inList referencingChats }
            .andWhere { Chats.ownerId eq UUID.fromString(userId) }
            .count() > 0
        return isUserPartOfReferencingChat
    }
}
