package illyan.butler.repository.message

import illyan.butler.model.DomainMessage
import illyan.butler.repository.user.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.annotation.Single

@Single
class MessageMemoryRepository(
    private val userRepository: UserRepository
) : MessageRepository {
    private val messages = mutableMapOf<String, DomainMessage>()
    private val chatMessages = mutableMapOf<String, List<DomainMessage>>()
    private val userMessages = mutableMapOf<String, List<DomainMessage>>()

    private val messageStateFlows = mutableMapOf<String, MutableStateFlow<Pair<DomainMessage?, Boolean>>>()
    override fun getMessageFlow(messageId: String): StateFlow<Pair<DomainMessage?, Boolean>> {
        return messageStateFlows.getOrPut(messageId) {
            MutableStateFlow(messages[messageId] to false)
        }
    }

    private val chatMessageStateFlows = mutableMapOf<String, MutableStateFlow<Pair<List<DomainMessage>?, Boolean>>>()

    override fun getChatMessagesFlow(chatId: String): StateFlow<Pair<List<DomainMessage>?, Boolean>> {
        return chatMessageStateFlows.getOrPut(chatId) {
            MutableStateFlow(chatMessages[chatId] to false)
        }
    }

    private val userMessageStateFlows = mutableMapOf<String, MutableStateFlow<Pair<List<DomainMessage>?, Boolean>>>()
    override fun getUserMessagesFlow(userId: String): StateFlow<Pair<List<DomainMessage>?, Boolean>> {
        return userMessageStateFlows.getOrPut(userId) {
            MutableStateFlow(userMessages[userId] to false)
        }
    }

    override suspend fun upsert(message: DomainMessage): String {
        val newMessage = if (message.id == null) {
            message.copy(id = (messages.size + 1).toString())
        } else message

        messages[newMessage.id!!] = newMessage
        messageStateFlows[newMessage.id]?.update { newMessage to false }
        val userId = userRepository.signedInUserId.value!!
        userMessages[userId] = userMessages[userId]?.plus(newMessage) ?: listOf(newMessage)
        userMessageStateFlows[userId]?.update { userMessages[userId] to false }

        return newMessage.id
    }
}