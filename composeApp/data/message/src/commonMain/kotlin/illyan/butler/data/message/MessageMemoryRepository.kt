package illyan.butler.data.message

import illyan.butler.data.settings.AppRepository
import illyan.butler.domain.model.DomainMessage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single

@Single
class MessageMemoryRepository(
    private val appRepository: AppRepository
) : MessageRepository {
    private val messages = mutableMapOf<String, DomainMessage>()
    private val chatMessages = mutableMapOf<String, List<DomainMessage>>()
    private val userMessages = mutableMapOf<String, List<DomainMessage>>()

    private val messageStateFlows = mutableMapOf<String, MutableStateFlow<DomainMessage?>>()
    override fun getMessageFlow(messageId: String, deviceOnly: Boolean): Flow<DomainMessage?> {
        return messageStateFlows.getOrPut(messageId) {
            MutableStateFlow(messages[messageId])
        }
    }

    private val chatMessageStateFlows = mutableMapOf<String, MutableStateFlow<List<DomainMessage>>>()

    override fun getChatMessagesFlow(chatId: String, deviceOnly: Boolean): Flow<List<DomainMessage>> {
        return chatMessageStateFlows.getOrPut(chatId) {
            MutableStateFlow(chatMessages[chatId]!!)
        }
    }

    private val userMessageStateFlows = mutableMapOf<String, MutableStateFlow<List<DomainMessage>>>()
    override fun getUserMessagesFlow(userId: String, deviceOnly: Boolean): Flow<List<DomainMessage>> {
        return userMessageStateFlows.getOrPut(userId) {
            MutableStateFlow(userMessages.getOrPut(userId) { emptyList() })
        }
    }

    override suspend fun upsert(message: DomainMessage, deviceOnly: Boolean): String {
        val newMessage = if (message.id == null) {
            message.copy(
                id = (messages.size + 1).toString(),
                time = Clock.System.now().toEpochMilliseconds()
            )
        } else message

        messages[newMessage.id!!] = newMessage
        messageStateFlows[newMessage.id]?.update { newMessage }
        val userId = if (deviceOnly) {
            appRepository.appSettings.first()!!.clientId
        } else {
            appRepository.currentSignedInUserId.first()!!
        }
        userMessages[userId] = userMessages[userId]?.plus(newMessage) ?: listOf(newMessage)
        userMessageStateFlows[userId]?.update { userMessages[userId]!! }

        return newMessage.id!!
    }

    override suspend fun delete(message: DomainMessage, deviceOnly: Boolean) {
        messages.remove(message.id)
        messageStateFlows[message.id]?.update { null }
        val userId = if (deviceOnly) {
            appRepository.appSettings.first()!!.clientId
        } else {
            appRepository.currentSignedInUserId.first()!!
        }
        userMessages[userId] = userMessages[userId]?.filter { it.id != message.id } ?: emptyList()
        userMessageStateFlows[userId]?.update { userMessages[userId]!! }
    }
}
