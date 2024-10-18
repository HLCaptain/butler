package illyan.butler.data.sync.store.builder

import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.sync.store.key.MessageKey
import illyan.butler.data.sync.store.provideBookkeeper
import illyan.butler.domain.model.DomainMessage
import illyan.butler.utils.randomUUID
import kotlinx.coroutines.flow.map
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
    fetcher = Fetcher.ofFlow { key ->
        require(key is MessageKey.Read.ByMessageId)
        messageNetworkDataSource.fetchById(key.messageId).map { it.toDomainModel() }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is MessageKey.Read.ByMessageId)
            messageLocalDataSource.getMessageById(key.messageId)
        },
        writer = { key, local ->
            when (key) {
                is MessageKey.Write.Create -> messageLocalDataSource.upsertMessage(local.copy(id = randomUUID()))
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
            val message = output.toNetworkModel()
            val newMessage = when (key) {
                is MessageKey.Write.Create -> messageNetworkDataSource.upsert(message).also {
                    messageLocalDataSource.replaceMessage(it.id!!, it.toDomainModel())
                }
                is MessageKey.Write.Upsert -> messageNetworkDataSource.upsert(message)
            }
            UpdaterResult.Success.Typed(newMessage.toDomainModel())
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        DomainMessage::class.qualifiedName.toString()
    ) { it.toString() }
)