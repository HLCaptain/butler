package illyan.butler.repository.chat

import illyan.butler.data.sync.store.builder.ChatMutableStoreBuilder
import illyan.butler.data.sync.store.builder.UserChatStoreBuilder
import illyan.butler.data.sync.store.key.ChatKey
import illyan.butler.di.KoinNames
import illyan.butler.manager.AuthManager
import illyan.butler.manager.HostManager
import illyan.butler.model.DomainChat
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

@OptIn(ExperimentalStoreApi::class)
@Single
class ChatStoreRepository(
    chatMutableStoreBuilder: ChatMutableStoreBuilder,
    userChatStoreBuilder: UserChatStoreBuilder,
    @Named(KoinNames.CoroutineScopeIO) private val coroutineScopeIO: CoroutineScope,
    private val hostManager: HostManager,
    private val authManager: AuthManager
) : ChatRepository {
    @OptIn(ExperimentalStoreApi::class)
    val chatMutableStore = chatMutableStoreBuilder.store

    val userChatStore = userChatStoreBuilder.store
    override suspend fun deleteAllChats(userId: String) {
        userChatStore.clear(ChatKey.Delete.ByUserId(userId))
    }

    private val chatStateFlows = mutableMapOf<String, StateFlow<Pair<DomainChat?, Boolean>>>()
    private val userChatStateFlows = mutableMapOf<String, StateFlow<Pair<List<DomainChat>?, Boolean>>>()

    init {
        coroutineScopeIO.launch {
            hostManager.currentHost.collect {
                Napier.d("Host changed, clearing chat state flows")
                if (chatStateFlows.isNotEmpty()) chatStateFlows.clear()
                if (userChatStateFlows.isNotEmpty()) userChatStateFlows.clear()
            }
        }
        coroutineScopeIO.launch {
            authManager.isUserSignedIn.collect {
                Napier.d("User signed in status changed, clearing chat state flows")
                if (chatStateFlows.isNotEmpty()) chatStateFlows.clear()
                if (userChatStateFlows.isNotEmpty()) userChatStateFlows.clear()
            }
        }
    }

    @OptIn(ExperimentalStoreApi::class)
    override fun getChatFlow(chatId: String): StateFlow<Pair<DomainChat?, Boolean>> {
        return chatStateFlows.getOrPut(chatId) {
            chatMutableStore.stream<StoreReadResponse<DomainChat>>(
                StoreReadRequest.cached(ChatKey.Read.ByChatId(chatId), true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.qualifiedName}")
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

    override fun getUserChatsFlow(userId: String): StateFlow<Pair<List<DomainChat>?, Boolean>> {
        return userChatStateFlows.getOrPut(userId) {
            userChatStore.stream(
                StoreReadRequest.cached(ChatKey.Read.ByUserId(userId), true)
            ).map {
                it.throwIfError()
                Napier.d("Read Response: ${it::class.qualifiedName}")
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
        val writtenChat = chatMutableStore.write(
            StoreWriteRequest.of(
                key = if (chat.id == null) ChatKey.Write.Create else ChatKey.Write.Upsert,
                value = chat,
            )
        )
        Napier.d("Chat upserted: $writtenChat")
        if (writtenChat is StoreWriteResponse.Success.Typed<*>) {
            val domainChat = (writtenChat as? StoreWriteResponse.Success.Typed<DomainChat>)?.value
            return domainChat?.id!!
        } else {
            throw IllegalStateException("Chat upsert failed")
        }
    }

    override suspend fun deleteChat(chatId: String) {
        chatMutableStore.clear(ChatKey.Delete.ByChatId(chatId))
    }
}
