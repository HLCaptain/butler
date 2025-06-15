package illyan.butler.data.message

import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.local.datasource.MessageLocalDataSource
import illyan.butler.core.network.datasource.MessageNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.core.sync.provideBookkeeper
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.MessageStatus
import illyan.butler.shared.model.chat.Source
import kotlinx.coroutines.flow.emptyFlow
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@Single
class MessageMutableStoreBuilder(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideMessageMutableStore(messageLocalDataSource, messageNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
fun provideMessageMutableStore(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow<MessageKey, Message> { key ->
        require(key is MessageKey.Read.ByMessageId)
        if (key.source is Source.Server) {
            messageNetworkDataSource.fetchById(key.source, key.messageId)
        } else {
            emptyFlow()
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is MessageKey.Read.ByMessageId)
            messageLocalDataSource.getMessageById(key.messageId)
        },
        writer = { key, local ->
            when (key) {
                MessageKey.Write.Create, MessageKey.Write.Upsert -> messageLocalDataSource.upsertMessage(local)
                is MessageKey.Read.ByMessageId -> messageLocalDataSource.upsertMessage(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key mimeType: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is MessageKey.Delete)
            if (!key.message.deviceOnly) {
                messageNetworkDataSource.delete(key.message)
            }
            messageLocalDataSource.deleteMessageById(key.message.id)
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
                is MessageKey.Write.Create -> if (output.deviceOnly) {
                    output.copy(status = MessageStatus.SENT) // Do not upload device-only messages
                } else {
                    messageNetworkDataSource.upsert(output).also {
                        messageLocalDataSource.replaceMessage(it.id, it)
                    }
                }
                is MessageKey.Write.Upsert -> if (output.deviceOnly) {
                    output // Do not upload device-only messages
                } else {
                    messageNetworkDataSource.upsert(output)
                }
            }
            UpdaterResult.Success.Typed(newMessage)
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        Message::class.qualifiedName.toString()
    ) { it.toString() }
)
