package illyan.butler.repository

import illyan.butler.data.store.ChatMessageKey
import illyan.butler.data.store.ChatMessageMutableStoreBuilder
import illyan.butler.data.store.MessageMutableStoreBuilder
import illyan.butler.di.NamedCoroutineScopeIO
import illyan.butler.domain.model.DomainMessage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest

@Single
class MessageRepository(
    messageMutableStoreBuilder: MessageMutableStoreBuilder,
    chatMessageMutableStoreBuilder: ChatMessageMutableStoreBuilder,
    @NamedCoroutineScopeIO private val coroutineScopeIO: CoroutineScope,
) {
    @OptIn(ExperimentalStoreApi::class)
    val messageMutableStore = messageMutableStoreBuilder.store
    @OptIn(ExperimentalStoreApi::class)
    val chatMessageMutableStore = chatMessageMutableStoreBuilder.store

    private val chatStateFlows = mutableMapOf<String, StateFlow<Pair<List<DomainMessage>?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    fun getChatFlow(uuid: String, limit: Int, timestamp: Long): StateFlow<Pair<List<DomainMessage>?, Boolean>> {
        return chatStateFlows.getOrPut(uuid) {
            chatMessageMutableStore.stream<StoreReadResponse<DomainMessage>>(
                StoreReadRequest.fresh(ChatMessageKey(uuid, limit, timestamp))
            ).map {
                it.throwIfError()
                Napier.d("Read Response: $it")
                val data = it.dataOrNull()
                Napier.d("Chat is $data")
                data to (it is StoreReadResponse.Loading)
            }.stateIn(
                coroutineScopeIO,
                SharingStarted.Eagerly,
                null to true
            )
        }
    }

    @OptIn(ExperimentalStoreApi::class)
    suspend fun upsert(message: DomainMessage) {
        messageMutableStore.write(
            StoreWriteRequest.of(
                key = message.id,
                value = message,
            )
        )
    }
}