package illyan.butler.data.store.builder

import illyan.butler.data.local.datasource.ChatLocalDataSource
import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.store.key.ChatKey
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder

@Single
class UserChatStoreBuilder(
    chatLocalDataSource: ChatLocalDataSource,
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
) {
    val store = provideUserChatStore(chatLocalDataSource, messageLocalDataSource, chatNetworkDataSource)
}

fun provideUserChatStore(
    chatLocalDataSource: ChatLocalDataSource,
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
) = StoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        require(key is ChatKey.Read.ByUserId)
        chatNetworkDataSource.fetchByUserId(key.userId).map { chats -> chats.map { it.toDomainModel() } }
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
                else -> throw IllegalArgumentException("Unsupported key type: ${key::class.qualifiedName}")
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