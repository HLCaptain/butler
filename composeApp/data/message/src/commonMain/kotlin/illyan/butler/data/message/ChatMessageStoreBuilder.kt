package illyan.butler.data.message

import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.network.datasource.MessageNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.Source
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder
import kotlin.uuid.ExperimentalUuidApi

@Single
class ChatMessageStoreBuilder(
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: MessageNetworkDataSource,
) {
    val store = provideChatMessageMutableStore(messageLocalDataSource, chatNetworkDataSource)
}

@OptIn(ExperimentalUuidApi::class)
fun provideChatMessageMutableStore(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
) = StoreBuilder.from(
    fetcher = Fetcher.ofFlow<MessageKey, List<Message>> { key ->
        require(key is MessageKey.Read.ByChatId)
        require(key.source is Source.Server)
        messageNetworkDataSource.fetchByChatId(key.source, key.chatId)
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
                else -> throw IllegalArgumentException("Unsupported key mimeType: ${key::class.qualifiedName}")
            }
        },
        deleteAll = {
            messageLocalDataSource.deleteAllMessages()
        }
    ),
    converter = NoopConverter(),
).build()