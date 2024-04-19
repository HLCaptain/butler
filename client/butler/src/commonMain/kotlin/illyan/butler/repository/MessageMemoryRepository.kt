package illyan.butler.repository

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.store.ChatMessageMutableStoreBuilder
import illyan.butler.data.store.MessageMutableStoreBuilder
import illyan.butler.data.store.UserMessageMutableStoreBuilder
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainMessage
import illyan.butler.manager.HostManager
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest

@Single
class MessageMemoryRepository : MessageRepository {
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

        return newMessage.id
    }
}