package illyan.butler.data.store

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.Message
import illyan.butler.domain.model.DomainMessage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
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
    databaseHelper: DatabaseHelper,
    chatNetworkDataSource: MessageNetworkDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideChatMessageMutableStore(databaseHelper, chatNetworkDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideChatMessageMutableStore(
    databaseHelper: DatabaseHelper,
    messageNetworkDataSource: MessageNetworkDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key: String ->
        Napier.d("Fetching messages $key")
        messageNetworkDataSource.fetchNewMessages()
            .map { messages -> messages.filter { it.chatId == key } }
            .filter { it.isNotEmpty() }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            databaseHelper.queryAsListFlow {
                Napier.d("Reading messages at $key")
                it.messageQueries.selectByChat(key)
            }.map { messages ->
                messages.map { it.toDomainModel() }
            }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                local.forEach {
                    Napier.d("Writing messages for user $key with $local")
                    db.messageQueries.upsert(it)
                }
            }
        },
        delete = { key ->
            databaseHelper.withDatabase {
                Napier.d("Deleting messages at $key")
                it.messageQueries.deleteAllChatMessagesForChat(key)
            }
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all messages")
                it.messageQueries.deleteAll()
            }
        }
    ),
    converter = Converter.Builder<List<MessageDto>, List<Message>, List<DomainMessage>>()
        .fromOutputToLocal { messages -> messages.map { it.toLocalModel() } }
        .fromNetworkToLocal { messages -> messages.map { it.toLocalModel() } }
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
        databaseHelper,
        DomainMessage::class.simpleName.toString()
    ) { it }
)