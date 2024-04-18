package illyan.butler.repository

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.store.ChatMutableStoreBuilder
import illyan.butler.data.store.UserChatMutableStoreBuilder
import illyan.butler.di.KoinNames
import illyan.butler.domain.model.DomainChat
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
class ChatRepository(
    chatMutableStoreBuilder: ChatMutableStoreBuilder,
    userChatMutableStoreBuilder: UserChatMutableStoreBuilder,
    private val chatNetworkDataSource: ChatNetworkDataSource,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
) {
    @OptIn(ExperimentalStoreApi::class)
    val chatMutableStore = chatMutableStoreBuilder.store

    @OptIn(ExperimentalStoreApi::class)
    val userChatMutableStore = userChatMutableStoreBuilder.store

    private val chatStateFlows = mutableMapOf<String, StateFlow<Pair<DomainChat?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    fun getChatFlow(chatId: String): StateFlow<Pair<DomainChat?, Boolean>> {
        return chatStateFlows.getOrPut(chatId) {
            chatMutableStore.stream<StoreReadResponse<DomainChat>>(
                StoreReadRequest.cached(chatId, true)
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

    private val userChatStateFlows = mutableMapOf<String, StateFlow<Pair<List<DomainChat>?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    fun getUserChatsFlow(userId: String): StateFlow<Pair<List<DomainChat>?, Boolean>> {
        return userChatStateFlows.getOrPut(userId) {
            userChatMutableStore.stream<StoreReadResponse<List<DomainChat>>>(
                StoreReadRequest.cached(userId, true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: $it")
                val data = it.dataOrNull()
                Napier.d("Chats are $data")
                data to (it is StoreReadResponse.Loading)
            }.stateIn(
                coroutineScopeIO,
                SharingStarted.Eagerly,
                null to true
            )
        }
    }

    @OptIn(ExperimentalStoreApi::class)
    suspend fun upsert(chat: DomainChat): String {
        val newChat = if (chat.id == null) {
            chatNetworkDataSource.upsert(chat.toNetworkModel()).toDomainModel()
        } else chat
        chatMutableStore.write(
            StoreWriteRequest.of(
                key = newChat.id!!,
                value = newChat,
            )
        )
        return newChat.id
    }
}