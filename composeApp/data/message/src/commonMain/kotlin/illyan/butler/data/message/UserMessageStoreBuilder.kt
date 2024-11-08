package illyan.butler.data.message

import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.network.datasource.MessageNetworkDataSource
import illyan.butler.core.sync.NoopConverter
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
        require(key is MessageKey.Read.ByUserId)
        messageNetworkDataSource.fetchAvailableToUser()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is MessageKey.Read.ByUserId) {
                "Expected key to be of type MessageKey.Read.ByUserId, but was ${key::class.qualifiedName}"
            }
            messageLocalDataSource.getAccessibleMessagesForUser(key.userId)
        },
        writer = { key, local ->
            when (key) {
                is MessageKey.Write.Upsert -> messageLocalDataSource.upsertMessages(local)
                is MessageKey.Read.ByUserId -> messageLocalDataSource.upsertMessages(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key type: ${key::class.qualifiedName}")
            }
        }
    ),
    converter = NoopConverter(),
).build()