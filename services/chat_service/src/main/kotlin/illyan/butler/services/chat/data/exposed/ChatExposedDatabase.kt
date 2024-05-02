package illyan.butler.services.chat.data.exposed

import illyan.butler.services.chat.data.db.ChatDatabase
import illyan.butler.services.chat.data.model.chat.ChatDto
import illyan.butler.services.chat.data.model.chat.MessageDto
import illyan.butler.services.chat.data.schema.ChatMembers
import illyan.butler.services.chat.data.schema.Chats
import illyan.butler.services.chat.data.schema.Messages
import io.github.aakira.napier.Napier
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
import org.jetbrains.exposed.sql.max
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.koin.core.annotation.Single

@Single
class ChatExposedDatabase(
    private val database: Database,
    private val dispatcher: CoroutineDispatcher
) : ChatDatabase {
    init {
        transaction(database) {
            SchemaUtils.create(Chats, ChatMembers)
        }
    }
    override suspend fun getChat(userId: String, chatId: String): ChatDto {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.memberId eq userId) and (ChatMembers.chatId eq chatId)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                Chats.selectAll().where(Chats.id eq chatId).first().toChatDto()
            } else {
                throw Exception("User is not in chat")
            }
        }
    }

    override suspend fun createChat(userId: String, chat: ChatDto): ChatDto {
        Napier.d("Creating chat $chat")
        return newSuspendedTransaction(dispatcher, database) {
            val createdMillis = Clock.System.now().toEpochMilliseconds()
            val chatId = Chats.insertAndGetId {
                it[name] = chat.name
                it[created] = createdMillis
                it[endpoints] = chat.aiEndpoints
            }
            (chat.members + userId).distinct().forEach { member ->
                ChatMembers.insertIgnore {
                    it[ChatMembers.chatId] = chatId
                    it[ChatMembers.memberId] = member
                }
            }
            chat.copy(id = chatId.value, created = createdMillis)
        }
    }

    override suspend fun editChat(userId: String, chat: ChatDto) {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.memberId eq userId) and (ChatMembers.chatId eq chat.id!!)
            val isUserInChat = ChatMembers.selectAll().where(userChat).count() > 0
            if (isUserInChat) {
                Chats.update({ Chats.id eq chat.id }) { it[name] = chat.name }
                val currentMembers =
                    ChatMembers.selectAll().where(ChatMembers.chatId eq chat.id).map { it[ChatMembers.memberId] }
                val newMembers = chat.members
                val removedMembers = currentMembers.filter { !newMembers.contains(it) }
                val addedMembers = newMembers.filter { !currentMembers.contains(it) }
                removedMembers.forEach { member ->
                    ChatMembers.deleteWhere { (chatId eq chat.id) and (ChatMembers.memberId eq member) }
                }
                addedMembers.forEach { member ->
                    ChatMembers.insertIgnore {
                        it[chatId] = chat.id
                        it[ChatMembers.memberId] = member
                    }
                }
            } else {
                throw Exception("User is not in chat")
            }
        }
    }

    override suspend fun deleteChat(userId: String, chatId: String): Boolean {
        return newSuspendedTransaction(dispatcher, database) {
            val userChat = (ChatMembers.memberId eq userId) and (ChatMembers.chatId eq chatId)
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
            val userChatIds = ChatMembers.selectAll().where { ChatMembers.memberId eq userId }
                .map { it[ChatMembers.chatId] }

            val relevantChatIds = Messages.selectAll().where {
                (Messages.chatId inList userChatIds) and
                        (Messages.time greaterEq fromDate) and
                        (Messages.time lessEq toDate)
            }.map { it[Messages.chatId] }.distinct()

            val chats = Chats.selectAll().where {
                (Chats.id inList relevantChatIds) or
                        ((Chats.id notInList relevantChatIds) and
                                (Chats.created greaterEq fromDate) and
                                (Chats.created lessEq toDate))
            }.mapNotNull { chatRow -> chatRow.toChatDto() }
            chats
        }
    }

    override suspend fun getChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChatQuery = ChatMembers.selectAll().where { ChatMembers.memberId eq userId }
            val userChatReferences = userChatQuery.map { it[ChatMembers.chatId] }
            val userChats = userChatQuery.map { it.toChatDto() }

            // Take the max of Chat.created and the max of Message.time for each Chat
            val chatAndLastMessageTime = Messages
                .select(Messages.time.max())
                .groupBy(Messages.chatId)
                .where { (Messages.chatId inList userChatReferences) }
                .asSequence()
                .sortedBy { Messages.time }
                .associate { it[Messages.chatId].value to it[Messages.time] }

            val chats = userChats
                .sortedBy { chatAndLastMessageTime[it.id] ?: it.created }
                .drop(offset)
                .take(limit)
            chats
        }
    }

    override suspend fun getChats(userId: String): List<ChatDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChatQuery = ChatMembers.selectAll().where { ChatMembers.memberId eq userId }
            val userChatReferences = userChatQuery.map { it[ChatMembers.chatId] }

            val relevantMessages = Messages
                .selectAll()
                .where { (Messages.chatId inList userChatReferences) }
                .map { it.toMessageDto() }.distinct()

            val chats = Chats.selectAll().where { Chats.id inList userChatReferences }.map { chatRow ->
                chatRow.toChatDto(relevantMessages.filter { it.chatId == chatRow[Chats.id].value })
            }
            chats
        }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, timestamp: Long): List<ChatDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChatQuery = ChatMembers.selectAll().where { ChatMembers.memberId eq userId }
            val userChatReferences = userChatQuery.map { it[ChatMembers.chatId] }

            val relevantChatIds = Messages
                .selectAll()
                .where { (Messages.chatId inList userChatReferences) and (Messages.time lessEq timestamp) }
                .sortedByDescending { Messages.time }
                .take(limit)
                .map { it[Messages.chatId].value }.distinct()

            val chats = Chats.selectAll()
                .where {
                    (Chats.id inList userChatReferences) or ((Chats.id notInList userChatReferences) and (Chats.created lessEq timestamp))
                }.map { chatRow -> chatRow.toChatDto() }
                .sortedByDescending { it.lastFewMessages.firstOrNull()?.time ?: it.created }
                .take(limit)
            chats
        }
    }

    override suspend fun getPreviousChats(userId: String, limit: Int, offset: Int): List<ChatDto> {
        return newSuspendedTransaction(dispatcher, database) {
            val userChatQuery = ChatMembers.selectAll().where { ChatMembers.memberId eq userId }
            val userChatReferences = userChatQuery.map { it[ChatMembers.chatId] }
            val userChats = userChatQuery.map { it.toChatDto() }

            // Take the max of Chat.created and the max of Message.time for each Chat
            val chatAndLastMessageTime = Messages
                .select(Messages.time.max())
                .groupBy(Messages.chatId)
                .where { (Messages.chatId inList userChatReferences) }
                .asSequence()
                .sortedBy { Messages.time }
                .associate { it[Messages.chatId].value to it[Messages.time] }

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

    private fun ResultRow.toChatDto(messages: List<MessageDto> = emptyList()) = ChatDto(
        id = this[Chats.id].value,
        created = this[Chats.created],
        name = this[Chats.name],
        members = ChatMembers.selectAll().where(ChatMembers.chatId eq this[Chats.id]).map { it[ChatMembers.memberId] },
        lastFewMessages = messages,
        aiEndpoints = this[Chats.endpoints]
    )
}