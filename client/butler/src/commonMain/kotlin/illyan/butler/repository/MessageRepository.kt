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

interface MessageRepository {
    fun getChatMessagesFlow(id: String): StateFlow<Pair<List<DomainMessage>?, Boolean>>
    suspend fun upsert(message: DomainMessage): String
    fun getUserMessagesFlow(userId: String): StateFlow<Pair<List<DomainMessage>?, Boolean>>
}