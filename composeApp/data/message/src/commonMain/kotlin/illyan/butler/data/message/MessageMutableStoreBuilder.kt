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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class MessageMutableStoreBuilder(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideMessageMutableStore(messageLocalDataSource, messageNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class)
fun provideMessageMutableStore(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
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
                is MessageKey.Write.Create -> messageLocalDataSource.upsertMessage(local.copy(id = Uuid.random().toString()))
                is MessageKey.Write.Upsert -> messageLocalDataSource.upsertMessage(local)
                is MessageKey.Read.ByMessageId -> messageLocalDataSource.upsertMessage(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key type: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is MessageKey.Delete.ByMessageId)
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