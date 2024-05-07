package illyan.butler.repository.message

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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest

@Single
class MessageStoreRepository(
    messageMutableStoreBuilder: MessageMutableStoreBuilder,
    chatMessageMutableStoreBuilder: ChatMessageMutableStoreBuilder,
    userMessageMutableStoreBuilder: UserMessageMutableStoreBuilder,
    private val messageNetworkDataSource: MessageNetworkDataSource,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
    private val hostManager: HostManager
) : MessageRepository {
    @OptIn(ExperimentalStoreApi::class)
    val messageMutableStore = messageMutableStoreBuilder.store
    @OptIn(ExperimentalStoreApi::class)
    val chatMessageMutableStore = chatMessageMutableStoreBuilder.store
    @OptIn(ExperimentalStoreApi::class)
    val userMessageMutableStore = userMessageMutableStoreBuilder.store
    @OptIn(ExperimentalStoreApi::class)
    override suspend fun deleteAllMessages(userId: String) {
        userMessageMutableStore.clear(userId)
    }

    init {
        coroutineScopeIO.launch {
            hostManager.currentHost.collect {
                Napier.d("Host changed, clearing message state flows")
                chatMessagesStateFlows.clear()
                userMessageStateFlows.clear()
            }
        }
    }

    private val messageStateFlows = mutableMapOf<String, StateFlow<Pair<DomainMessage?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    override fun getMessageFlow(messageId: String): StateFlow<Pair<DomainMessage?, Boolean>> {
        return messageStateFlows.getOrPut(messageId) {
            messageMutableStore.stream<StoreReadResponse<DomainMessage>>(
                StoreReadRequest.cached(messageId, true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.simpleName}")
                val data = it.dataOrNull()
                Napier.d("Message: $data")
                data to (it is StoreReadResponse.Loading)
            }.stateIn(
                coroutineScopeIO,
                SharingStarted.Eagerly,
                null to true
            )
        }
    }

    private val chatMessagesStateFlows = mutableMapOf<String, StateFlow<Pair<List<DomainMessage>?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    override fun getChatMessagesFlow(chatId: String): StateFlow<Pair<List<DomainMessage>?, Boolean>> {
        return chatMessagesStateFlows.getOrPut(chatId) {
            chatMessageMutableStore.stream<StoreReadResponse<List<DomainMessage>>>(
                StoreReadRequest.cached(chatId, true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.simpleName}")
                val data = it.dataOrNull()
                Napier.d("Last 5 messages: ${data?.map { message -> message.id }?.takeLast(5)}")
                data to (it is StoreReadResponse.Loading)
            }.stateIn(
                coroutineScopeIO,
                SharingStarted.Eagerly,
                null to true
            )
        }
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun upsert(message: DomainMessage): String {
        val newMessage = if (message.id == null) {
            messageNetworkDataSource.upsert(message.toNetworkModel()).toDomainModel()
        } else message
        messageMutableStore.write(
            StoreWriteRequest.of(
                key = newMessage.id!!,
                value = newMessage,
            )
        )
        return newMessage.id
    }

    private val userMessageStateFlows = mutableMapOf<String, StateFlow<Pair<List<DomainMessage>?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    override fun getUserMessagesFlow(userId: String): StateFlow<Pair<List<DomainMessage>?, Boolean>> {
        return userMessageStateFlows.getOrPut(userId) {
            userMessageMutableStore.stream<StoreReadResponse<List<DomainMessage>>>(
                StoreReadRequest.cached(userId, true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.simpleName}")
                val data = it.dataOrNull()
                Napier.d("Last 5 messages: ${data?.map { message -> message.id }?.takeLast(5)}")
                data to (it is StoreReadResponse.Loading)
            }.stateIn(
                coroutineScopeIO,
                SharingStarted.Eagerly,
                null to true
            )
        }
    }
}