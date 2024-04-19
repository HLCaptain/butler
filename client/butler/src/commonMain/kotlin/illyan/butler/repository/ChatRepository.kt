package illyan.butler.repository

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.store.ChatMutableStoreBuilder
import illyan.butler.data.store.UserChatMutableStoreBuilder
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainChat
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

interface ChatRepository {
    fun getChatFlow(chatId: String): StateFlow<Pair<DomainChat?, Boolean>>
    fun getUserChatsFlow(userId: String): StateFlow<Pair<List<DomainChat>?, Boolean>>
    suspend fun upsert(chat: DomainChat): String
}