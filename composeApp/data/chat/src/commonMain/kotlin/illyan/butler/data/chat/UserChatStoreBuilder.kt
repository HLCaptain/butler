package illyan.butler.data.chat

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.network.datasource.ChatNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder

@Single
class UserChatStoreBuilder(
    chatLocalDataSource: ChatLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
) {
    val store = provideUserChatStore(chatLocalDataSource, chatNetworkDataSource)
}

fun provideUserChatStore(
    chatLocalDataSource: ChatLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
) = StoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        require(key is ChatKey.Read.ByUserId)
        chatNetworkDataSource.fetchByUserId(key.userId)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is ChatKey.Read.ByUserId)
            chatLocalDataSource.getChatsByUser(key.userId)
        },
        writer = { key, local ->
            when (key) {
                is ChatKey.Write.Upsert -> chatLocalDataSource.upsertChats(local)
                is ChatKey.Read.ByUserId -> chatLocalDataSource.upsertChats(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key mimeType: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is ChatKey.Delete.ByUserId)
            chatLocalDataSource.deleteChatsForUser(key.userId)
        },
        deleteAll = {
            chatLocalDataSource.deleteAllChats()
        }
    ),
    converter = NoopConverter(),
).build()