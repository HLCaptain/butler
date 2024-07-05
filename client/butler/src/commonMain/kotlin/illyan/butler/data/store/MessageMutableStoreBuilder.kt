package illyan.butler.data.store

import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.db.Message
import illyan.butler.domain.model.DomainMessage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.OnUpdaterCompletion
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
        Napier.d("Fetching message $key")
        combine(
            flow { emit(listOf(messageNetworkDataSource.fetch(key))) },
            flow { emit(emptyList<MessageDto>()); emitAll(messageNetworkDataSource.fetchNewMessages()) }
        ) { message, newMessages ->
            (newMessages + message).distinctBy { it?.id }.firstOrNull { it?.id == key }
        }.filterNotNull()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            Napier.d("Reading message at $key")
            messageLocalDataSource.getMessageById(key)
        },
        writer = { key, local ->
            Napier.d("Writing message at $key with $local")
            messageLocalDataSource.upsertMessage(local.toDomainModel())
        },
        delete = { key ->
            Napier.d("Deleting message at $key")
            messageLocalDataSource.getMessageById(key).first()?.chatId?.let {
                messageLocalDataSource.deleteMessage(key)
                messageNetworkDataSource.delete(key, it)
            }
        },
        deleteAll = {
            Napier.d("Deleting all messages")
            messageLocalDataSource.deleteAllMessages()
        }
    ),
    converter = Converter.Builder<MessageDto, Message, DomainMessage>()
        .fromOutputToLocal { it.toLocalModel() }
        .fromNetworkToLocal { it.toLocalModel() }
        .build(),
).build(
    updater = Updater.by(
        post = { key, output ->
            val response = messageNetworkDataSource.upsert(output.toNetworkModel()).toDomainModel()
            UpdaterResult.Success.Typed(response)
        },
        onCompletion = OnUpdaterCompletion(
            onSuccess = { success ->
                Napier.d("Successfully updated message")
            },
            onFailure = { _ ->
                Napier.d("Failed to update message")
            }
        )
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        Message::class.simpleName.toString()
    ) { it }
)