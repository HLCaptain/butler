package illyan.butler.data.message

import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.network.datasource.MessageNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder

@Single
class ChatMessageStoreBuilder(
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: MessageNetworkDataSource,
) {
    val store = provideChatMessageMutableStore(messageLocalDataSource, chatNetworkDataSource)
}

fun provideChatMessageMutableStore(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
) = StoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        require(key is MessageKey.Read.ByChatId)
        messageNetworkDataSource.fetchByChatId(key.chatId)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is MessageKey.Read.ByChatId)
            messageLocalDataSource.getMessagesByChatId(key.chatId)
        },
        writer = { key, local ->
            when (key) {
                is MessageKey.Write.Create -> messageLocalDataSource.upsertMessages(local)
                is MessageKey.Write.Upsert -> messageLocalDataSource.upsertMessages(local)
                is MessageKey.Read.ByChatId -> messageLocalDataSource.upsertMessages(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key type: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is MessageKey.Delete.ByChatId)
            messageLocalDataSource.deleteAllMessagesForChat(key.chatId)
        },
        deleteAll = {
            messageLocalDataSource.deleteAllMessages()
        }
    ),
    converter = NoopConverter(),
).build()