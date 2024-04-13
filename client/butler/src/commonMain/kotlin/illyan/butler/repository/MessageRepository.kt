package illyan.butler.repository

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.store.ChatMessageMutableStoreBuilder
import illyan.butler.data.store.MessageMutableStoreBuilder
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainMessage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest

@Single
class MessageRepository(
    messageMutableStoreBuilder: MessageMutableStoreBuilder,
    chatMessageMutableStoreBuilder: ChatMessageMutableStoreBuilder,
    private val messageNetworkDataSource: MessageNetworkDataSource,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
) {
    @OptIn(ExperimentalStoreApi::class)
    val messageMutableStore = messageMutableStoreBuilder.store
    @OptIn(ExperimentalStoreApi::class)
    val chatMessageMutableStore = chatMessageMutableStoreBuilder.store

    private val chatStateFlows = mutableMapOf<String, StateFlow<Pair<List<DomainMessage>?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    fun getChatFlow(id: String): StateFlow<Pair<List<DomainMessage>?, Boolean>> {
        return chatStateFlows.getOrPut(id) {
            chatMessageMutableStore.stream<StoreReadResponse<List<DomainMessage>>>(
                StoreReadRequest.fresh(id)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: $it")
                val data = it.dataOrNull()
                Napier.d("Messages are $data")
                data to (it is StoreReadResponse.Loading)
            }.stateIn(
                coroutineScopeIO,
                SharingStarted.Eagerly,
                null to true
            )
        }
    }

    @OptIn(ExperimentalStoreApi::class)
    suspend fun upsert(message: DomainMessage): String {
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
}