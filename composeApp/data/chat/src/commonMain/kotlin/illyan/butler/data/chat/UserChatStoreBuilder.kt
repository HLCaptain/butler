package illyan.butler.data.chat

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.network.datasource.ChatNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.uuid.ExperimentalUuidApi

@Single
class UserChatStoreBuilder(
    chatLocalDataSource: ChatLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
) {
    val store = provideUserChatStore(chatLocalDataSource, chatNetworkDataSource)
}

@OptIn(ExperimentalUuidApi::class)
fun provideUserChatStore(
    chatLocalDataSource: ChatLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
) = StoreBuilder.from(
    fetcher = Fetcher.ofFlow<ChatKey, List<Chat>> { key ->
        require(key is ChatKey.Read.BySource)
        require(key.source is Source.Server)
        chatNetworkDataSource.fetchByUserId(key.source)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is ChatKey.Read.BySource)
            chatLocalDataSource.getChatsBySource(key.source)
        },
        writer = { key, local ->
            when (key) {
                is ChatKey.Write.Upsert -> chatLocalDataSource.upsertChats(local)
                is ChatKey.Read.BySource -> chatLocalDataSource.upsertChats(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key mimeType: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is ChatKey.Delete)
            if (!key.chat.deviceOnly) {
                chatNetworkDataSource.delete(key.chat)
            }
            chatLocalDataSource.deleteChatById(key.chat.id)
        },
        deleteAll = {
            chatLocalDataSource.deleteAllChats()
        }
    ),
    converter = NoopConverter(),
).build()