package illyan.butler.data.message

import illyan.butler.data.settings.AppRepository
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Single
class MessageMemoryRepository(
    private val appRepository: AppRepository
) : MessageRepository {
    private val messages = mutableMapOf<Uuid, Message>()
    private val chatMessages = mutableMapOf<Uuid, List<Message>>()
    private val userMessages = mutableMapOf<Uuid, List<Message>>()
    private val chatMessageStateFlows = mutableMapOf<Uuid, MutableStateFlow<List<Message>>>()
    private val messageStateFlows = mutableMapOf<Uuid, MutableStateFlow<Message?>>()
    private val userMessageStateFlows = mutableMapOf<Uuid, MutableStateFlow<List<Message>>>()

    override fun getMessageFlow(messageId: Uuid, source: Source): Flow<Message?> {
        return messageStateFlows.getOrPut(messageId) {
            MutableStateFlow(messages[messageId])
        }
    }

    override fun getChatMessagesFlow(chatId: Uuid, source: Source): Flow<List<Message>> {
        return chatMessageStateFlows.getOrPut(chatId) {
            MutableStateFlow(chatMessages[chatId]!!)
        }
    }

    override fun getMessagesBySource(source: Source): Flow<List<Message>> {
        val userId = when (source) {
            is Source.Device -> source.deviceId
            is Source.Server -> source.userId
        }
        return userMessageStateFlows.getOrPut(userId) {
            MutableStateFlow(userMessages.getOrPut(userId) { emptyList() })
        }
    }

    override suspend fun create(message: Message): Uuid {
        return upsert(message)
    }

    override suspend fun upsert(message: Message): Uuid {
        val newMessage = message

        messages[newMessage.id] = newMessage
        messageStateFlows[newMessage.id]?.update { newMessage }
        val userId = when (newMessage.source) {
            is Source.Device -> (newMessage.source as Source.Device).deviceId
            is Source.Server -> (newMessage.source as Source.Server).userId
        }
        userMessages[userId] = userMessages[userId]?.plus(newMessage) ?: listOf(newMessage)
        userMessageStateFlows[userId]?.update { userMessages[userId]!! }

        return newMessage.id
    }

    override suspend fun delete(message: Message) {
        messages.remove(message.id)
        messageStateFlows[message.id]?.update { null }
        val userId = when (message.source) {
            is Source.Device -> (message.source as Source.Device).deviceId
            is Source.Server -> (message.source as Source.Server).userId
        }
        userMessages[userId] = userMessages[userId]?.filter { it.id != message.id } ?: emptyList()
        userMessageStateFlows[userId]?.update { userMessages[userId]!! }
    }
}
