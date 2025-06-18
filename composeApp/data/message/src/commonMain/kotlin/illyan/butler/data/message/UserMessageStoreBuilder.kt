package illyan.butler.data.message

import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.network.datasource.MessageNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.shared.model.chat.Source
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder

@Single
class UserMessageStoreBuilder(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
) {
    val store = provideUserMessageStore(messageLocalDataSource, messageNetworkDataSource)
}

fun provideUserMessageStore(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
) = StoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        require(key is MessageKey.Read.BySource)
        require(key.source is Source.Server)
        messageNetworkDataSource.fetchNewMessages(key.source)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is MessageKey.Read.BySource) {
                "Expected key to be of mimeType MessageKey.Read.BySource, but was ${key::class.qualifiedName}"
            }
            messageLocalDataSource.getMessagesBySource(key.source)
        },
        writer = { key, local ->
            when (key) {
                is MessageKey.Write.Upsert -> messageLocalDataSource.upsertMessages(local)
                is MessageKey.Read.BySource -> messageLocalDataSource.upsertMessages(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key mimeType: ${key::class.qualifiedName}")
            }
        }
    ),
    converter = NoopConverter(),
).build()