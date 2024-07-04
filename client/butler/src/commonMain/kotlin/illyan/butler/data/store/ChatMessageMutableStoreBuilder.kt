package illyan.butler.data.store

import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.domain.model.DomainMessage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
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
class ChatMessageMutableStoreBuilder(
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: MessageNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideChatMessageMutableStore(messageLocalDataSource, chatNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideChatMessageMutableStore(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key: String ->
        Napier.d("Fetching messages $key")
        combine(
            flow { emit(messageNetworkDataSource.fetchByChat(key)) }, // initial chat messages fetch
            flow { emit(emptyList<MessageDto>()); emitAll(messageNetworkDataSource.fetchNewMessages()) } // new messages fetch
        ) { messages, newMessages ->
            Napier.d("Fetched ${(messages + newMessages).distinctBy { it.id }.size} messages")
            (messages + newMessages)
                .distinctBy { it.id }
                .filter { it.chatId == key }
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            Napier.d("Reading messages at $key")
            messageLocalDataSource.getAllMessagesForChat(key)
        },
        writer = { key, local ->
            Napier.d("Writing messages for user $key with ${local.size} messages")
            messageLocalDataSource.insertMessages(local)
        },
        delete = { key ->
            Napier.d("Deleting messages at $key")
            messageLocalDataSource.deleteAllMessagesForChat(key)
        },
        deleteAll = {
            Napier.d("Deleting all messages")
            messageLocalDataSource.deleteAllMessages()
        }
    ),
    converter = Converter.Builder<List<MessageDto>, List<DomainMessage>, List<DomainMessage>>()
        .fromOutputToLocal { it }
        .fromNetworkToLocal { messages -> messages.map { it.toDomainModel() } }
        .build(),
).build(
    updater = Updater.by(
        post = { key, output ->
            val response = output.map { messageNetworkDataSource.upsert(it.toNetworkModel()).toDomainModel() }
            UpdaterResult.Success.Typed(response)
        },
        onCompletion = OnUpdaterCompletion(
            onSuccess = { _ ->
                Napier.d("Successfully updated messages")
            },
            onFailure = { _ ->
                Napier.d("Failed to update messages")
            }
        )
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        DomainMessage::class.simpleName.toString()
    ) { it }
)