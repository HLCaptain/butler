package illyan.butler.repository.message

import illyan.butler.data.store.builder.ChatMessageStoreBuilder
import illyan.butler.data.store.builder.MessageMutableStoreBuilder
import illyan.butler.data.store.builder.UserMessageStoreBuilder
import illyan.butler.data.store.key.MessageKey
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
import org.mobilenativefoundation.store.store5.StoreWriteResponse

@Single
class MessageStoreRepository(
    messageMutableStoreBuilder: MessageMutableStoreBuilder,
    chatMessageStoreBuilder: ChatMessageStoreBuilder,
    userMessageStoreBuilder: UserMessageStoreBuilder,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
    private val hostManager: HostManager
) : MessageRepository {
    @OptIn(ExperimentalStoreApi::class)
    val messageMutableStore = messageMutableStoreBuilder.store
    val chatMessageMutableStore = chatMessageStoreBuilder.store
    private val userMessageStore = userMessageStoreBuilder.store

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
                StoreReadRequest.cached(MessageKey.Read.ByMessageId(messageId), true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.qualifiedName}")
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
    override fun getChatMessagesFlow(chatId: String): StateFlow<Pair<List<DomainMessage>?, Boolean>> {
        return chatMessagesStateFlows.getOrPut(chatId) {
            chatMessageMutableStore.stream(
                StoreReadRequest.cached(MessageKey.Read.ByChatId(chatId), true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.qualifiedName}")
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
        return (messageMutableStore.write(
            StoreWriteRequest.of(
                key = if (message.id == null) MessageKey.Write.Create else MessageKey.Write.Upsert,
                value = message,
            )
        ) as? StoreWriteResponse.Success.Typed<DomainMessage>)?.value?.id!!
    }

    private val userMessageStateFlows = mutableMapOf<String, StateFlow<Pair<List<DomainMessage>?, Boolean>>>()
    override fun getUserMessagesFlow(userId: String): StateFlow<Pair<List<DomainMessage>?, Boolean>> {
        return userMessageStateFlows.getOrPut(userId) {
            userMessageStore.stream(
                StoreReadRequest.cached(MessageKey.Read.ByUserId(userId), true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.qualifiedName}")
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