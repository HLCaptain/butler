package illyan.butler.server.data.exposed

import illyan.butler.server.data.db.ChatDatabase
import illyan.butler.server.data.schema.Chats
import illyan.butler.server.data.schema.Messages
import illyan.butler.server.data.service.ApiException
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.response.StatusCode
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.SqlExpressionBuilder.eq
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.max
import org.jetbrains.exposed.v1.core.or
import org.jetbrains.exposed.v1.r2dbc.R2dbcDatabase
import org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import org.jetbrains.exposed.v1.r2dbc.deleteWhere
import org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import org.jetbrains.exposed.v1.r2dbc.select
import org.jetbrains.exposed.v1.r2dbc.selectAll
import org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.r2dbc.update
import org.koin.core.annotation.Single
import java.util.UUID

@Single
class ChatExposedDatabase(
    private val database: R2dbcDatabase,
    private val dispatcher: CoroutineDispatcher,
    coroutineScopeIO: CoroutineScope
) : ChatDatabase {

    init {
        coroutineScopeIO.launch {
            suspendTransaction(dispatcher, db = database) {
                SchemaUtils.create(Chats)
            }
        }
    }

    override suspend fun getChat(userId: String, chatId: String): ChatDto {
        return suspendTransaction(dispatcher, db = database) {
            val userChat = Chats.selectAll()
                .where { Chats.id eq UUID.fromString(chatId) }
            val isUserInChat = userChat.map { it[Chats.ownerId] }.first().value.toString() == userId
            if (isUserInChat) {
                val chatUuid = UUID.fromString(chatId)
                Chats.selectAll().where(Chats.id eq chatUuid).first().toChatDto().also {
                    Napier.d("Returning chat $it")
                }
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
        }
    }

    override suspend fun createChat(userId: String, chat: ChatDto): ChatDto {
        Napier.d("Creating chat $chat")
        return suspendTransaction(dispatcher, db = database) {
            val createdMillis = Clock.System.now().toEpochMilliseconds()
            val chatId = Chats.insertAndGetId {
                it[name] = chat.name
                it[created] = createdMillis
                it[models] = chat.models
                it[summary] = chat.summary
                it[ownerId] = UUID.fromString(userId)
            }
            chat.copy(
                id = chatId.value.toString(),
                created = createdMillis,
                ownerId = userId
            )
        }
    }

    override suspend fun editChat(userId: String, chat: ChatDto): ChatDto {
        return suspendTransaction(dispatcher, db = database) {
            val chatUuid = UUID.fromString(chat.id)
            val userChat = Chats.selectAll()
                .where { Chats.id eq chatUuid }
            val isUserInChat = userChat.map { it[Chats.ownerId] }.first().value.toString() == userId
            if (isUserInChat) {
                Chats.update({ Chats.id eq chatUuid }) {
                    it[name] = chat.name
                    it[summary] = chat.summary
                    it[models] = chat.models
                }
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
            chat
        }
    }

    override suspend fun deleteChat(userId: String, chatId: String): Boolean {
        return suspendTransaction(dispatcher, db = database) {
            val chatUuid = UUID.fromString(chatId)
            val userChat = Chats.selectAll()
                .where { Chats.id eq chatUuid }
            val isUserInChat = userChat.map { it[Chats.ownerId] }.first().value.toString() == userId
            if (isUserInChat) {
                Chats.deleteWhere { id eq chatUuid } > 0 // Deleted more than 0 rows
            } else {
                throw ApiException(StatusCode.ChatNotFound)
            }
        }
    }

    override suspend fun getChats(userId: String, fromDate: Long, toDate: Long): List<ChatDto> {
        return suspendTransaction(dispatcher, db = database) {
            val userChatIds = Chats.selectAll().where { Chats.ownerId eq UUID.fromString(userId) }.map { it[Chats.id] }.toList()

            val relevantChatIds = Messages.selectAll().where {
                (Messages.chatId inList userChatIds) and
                        (Messages.time greaterEq fromDate) and
                        (Messages.time lessEq toDate)
            }.map { it[Messages.chatId] }.toList().distinct()

            val chats = Chats.selectAll().where {
                (Chats.id inList relevantChatIds) or
                        ((Chats.id notInList relevantChatIds) and
                                (Chats.created greaterEq fromDate) and
                                (Chats.created lessEq toDate))
            }.mapNotNull { chatRow -> chatRow.toChatDto() }.toList()
            chats
        }
    }

    override suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return suspendTransaction(dispatcher, db = database) {
            val userChatQuery = Chats.selectAll().where { Chats.ownerId eq UUID.fromString(userId) }
            val userChatReferences = userChatQuery.map { it[Chats.id] }.toList()
            val userChats = userChatQuery.map { it.toChatDto() }.toList()

            // Take the max of Chat.created and the max of Message.time for each Chat
            val chatAndLastMessageTime = Messages
                .select(Messages.time.max())
                .groupBy(Messages.chatId)
                .where { (Messages.chatId inList userChatReferences) }
                .toList()
                .sortedBy { Messages.time }
                .associate { it[Messages.chatId].value.toString() to it[Messages.time] }

            val chats = userChats
                .sortedBy { chatAndLastMessageTime[it.id] ?: it.created }
                .drop(offset)
                .take(limit)
            chats
        }
    }

    override suspend fun getChats(userId: String): List<ChatDto> {
        return suspendTransaction(dispatcher, db = database) {
            val userChatQuery = Chats.selectAll().where { Chats.ownerId eq UUID.fromString(userId) }
            val userChatReferences = userChatQuery.map { it[Chats.id] }.toList()

            val chats = Chats.selectAll().where { Chats.id inList userChatReferences }.map { chatRow ->
                chatRow.toChatDto()
            }.toList()
            chats
        }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto> {
        return suspendTransaction(dispatcher, db = database) {
            val userChatQuery = Chats.selectAll().where { Chats.ownerId eq UUID.fromString(userId) }
            val userChatReferences = userChatQuery.map { it[Chats.id] }.toList()

            val lastMessageOfChat = Messages
                .select(Messages.time.max())
                .groupBy(Messages.chatId)
                .where { (Messages.chatId inList userChatReferences) }
                .toList()
                .sortedBy { Messages.time }
                .associate { it[Messages.chatId].value.toString() to it[Messages.time] }

            val chats = Chats.selectAll()
                .where {
                    (Chats.id inList userChatReferences) or ((Chats.id notInList userChatReferences) and (Chats.created lessEq timestamp))
                }.map { chatRow -> chatRow.toChatDto() }
                .toList()
                .sortedByDescending { lastMessageOfChat[it.id] ?: it.created }
                .take(limit)
            chats
        }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return suspendTransaction(dispatcher, db = database) {
            val userChatQuery = Chats.selectAll().where { Chats.ownerId eq UUID.fromString(userId) }
            val userChatReferences = userChatQuery.map { it[Chats.id] }.toList()
            val userChats = userChatQuery.map { it.toChatDto() }.toList()

            // Take the max of Chat.created and the max of Message.time for each Chat
            val chatAndLastMessageTime = Messages
                .select(Messages.time.max())
                .groupBy(Messages.chatId)
                .where { (Messages.chatId inList userChatReferences) }
                .toList()
                .sortedBy { Messages.time }
                .associate { it[Messages.chatId].value.toString() to it[Messages.time] }

            val chats = userChats
                .sortedByDescending { chatAndLastMessageTime[it.id] ?: it.created }
                .drop(offset)
                .take(limit)
            chats
        }
    }

    override fun getChangedChatsAffectingUser(userId: String): Flow<List<ChatDto>> {
        return flow {
            var previousChats: Set<ChatDto>? = null
            while (true) {
                val chats = getChats(userId).toSet()
                val changedChats = previousChats?.let {
                    chats.filter { chat -> chat !in it }
                } ?: chats
                if (changedChats.isNotEmpty()) emit(changedChats.toList())
                previousChats = chats
                delay(10000)
            }
        }
    }

    override fun getChangesFromChat(userId: String, chatId: String): Flow<ChatDto> {
        return flow {
            var previousChat: ChatDto? = null
            while (true) {
                val chat = getChat(userId, chatId)
                if (chat != previousChat) emit(chat)
                previousChat = chat
                delay(10000)
            }
        }
    }

    private fun ResultRow.toChatDto() = ChatDto(
        id = this[Chats.id].value.toString(),
        created = this[Chats.created],
        name = this[Chats.name],
        ownerId = this[Chats.ownerId].value.toString(),
        models = this[Chats.models],
        summary = this[Chats.summary]
    )
}
