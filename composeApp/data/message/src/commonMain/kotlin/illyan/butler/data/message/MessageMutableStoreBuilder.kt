package illyan.butler.data.message

import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.network.datasource.MessageNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.core.sync.provideBookkeeper
import illyan.butler.domain.model.DomainMessage
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

@Single
class MessageMutableStoreBuilder(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideMessageMutableStore(messageLocalDataSource, messageNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideMessageMutableStore(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow<MessageKey, DomainMessage> { key ->
        require(key is MessageKey.Read.ByMessageId)
        messageNetworkDataSource.fetchById(key.messageId)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is MessageKey.Read.ByMessageId)
            messageLocalDataSource.getMessageById(key.messageId)
        },
        writer = { key, local ->
            when (key) {
                MessageKey.Write.Create, MessageKey.Write.Upsert, MessageKey.Write.DeviceOnly -> messageLocalDataSource.upsertMessage(local)
                is MessageKey.Read.ByMessageId -> messageLocalDataSource.upsertMessage(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key type: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is MessageKey.Delete)
            if (!key.deviceOnly) {
                messageNetworkDataSource.delete(key.messageId, key.chatId)
            }
            messageLocalDataSource.deleteMessageById(key.messageId)
        },
        deleteAll = {
            messageLocalDataSource.deleteAllMessages()
        }
    ),
    converter = NoopConverter(),
).build(
    updater = Updater.by(
        post = { key, output ->
            require(key is MessageKey.Write)
            val newMessage = when (key) {
                is MessageKey.Write.Create -> messageNetworkDataSource.upsert(output).also {
                    messageLocalDataSource.replaceMessage(it.id!!, it)
                }
                is MessageKey.Write.Upsert -> messageNetworkDataSource.upsert(output)
                is MessageKey.Write.DeviceOnly -> output // Do not upload device-only messages
            }
            UpdaterResult.Success.Typed(newMessage)
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        DomainMessage::class.qualifiedName.toString()
    ) { it.toString() }
)