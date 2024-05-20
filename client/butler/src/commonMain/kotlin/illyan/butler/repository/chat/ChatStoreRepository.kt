package illyan.butler.repository.chat

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

@OptIn(ExperimentalStoreApi::class)
@Single
class ChatStoreRepository(
    chatMutableStoreBuilder: ChatMutableStoreBuilder,
    userChatMutableStoreBuilder: UserChatMutableStoreBuilder,
    private val chatNetworkDataSource: ChatNetworkDataSource,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
    private val hostManager: HostManager
) : ChatRepository {
    @OptIn(ExperimentalStoreApi::class)
    val chatMutableStore = chatMutableStoreBuilder.store

    @OptIn(ExperimentalStoreApi::class)
    val userChatMutableStore = userChatMutableStoreBuilder.store
    override suspend fun deleteAllChats(userId: String) {
        userChatMutableStore.clear(userId)
    }

    init {
        coroutineScopeIO.launch {
            hostManager.currentHost.collect {
                Napier.d("Host changed, clearing chat state flows")
                chatStateFlows.clear()
                userChatStateFlows.clear()
            }
        }
    }

    private val chatStateFlows = mutableMapOf<String, StateFlow<Pair<DomainChat?, Boolean>>>()
    @OptIn(ExperimentalStoreApi::class)
    override fun getChatFlow(chatId: String): StateFlow<Pair<DomainChat?, Boolean>> {
        return chatStateFlows.getOrPut(chatId) {
            chatMutableStore.stream<StoreReadResponse<DomainChat>>(
                StoreReadRequest.cached(chatId, true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.simpleName}")
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
    override fun getUserChatsFlow(userId: String): StateFlow<Pair<List<DomainChat>?, Boolean>> {
        return userChatStateFlows.getOrPut(userId) {
            userChatMutableStore.stream<StoreReadResponse<List<DomainChat>>>(
                StoreReadRequest.cached(userId, true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.simpleName}")
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
    override suspend fun upsert(chat: DomainChat): String {
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

    override suspend fun deleteChat(chatId: String) {
        chatMutableStore.clear(chatId)
    }
}