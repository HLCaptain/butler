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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
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
class UserMessageMutableStoreBuilder(
    databaseHelper: DatabaseHelper,
    messageNetworkDataSource: MessageNetworkDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideUserMessageMutableStore(databaseHelper, messageNetworkDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideUserMessageMutableStore(
    databaseHelper: DatabaseHelper,
    messageNetworkDataSource: MessageNetworkDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key: String ->
        Napier.d("Fetching messages for user $key")
        combine(
            flow { emit(messageNetworkDataSource.fetchByUser(key)) },
            flow { emit(emptyList<MessageDto>()); emitAll(messageNetworkDataSource.fetchNewMessages()) }
        ) { messages, newMessages ->
            Napier.d("Fetched ${(messages + newMessages).distinct().size} messages")
            (messages + newMessages)
                .distinctBy { it.id }
                .filter { it.senderId == key }
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            databaseHelper.queryAsListFlow {
                Napier.d("Reading chat at $key")
                it.messageQueries.selectBySender(key)
            }.map { messages ->
                Napier.v { "Messages read: ${messages.size}" }
                messages.map { it.toDomainModel() }
            }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                Napier.d("Writing messages for user $key")
                local.forEach { db.messageQueries.upsert(it) }
            }
        },
        delete = { key ->
            databaseHelper.withDatabase {
                Napier.d("Deleting messages for user $key")
                it.messageQueries.selectBySender(key).executeAsList().forEach { message ->
                    messageNetworkDataSource.delete(message.id, message.chatId)
                }
                it.messageQueries.deleteBySender(key)
            }
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all chats")
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
            onSuccess = { success ->
                Napier.d("Successfully updated messages")

            },
            onFailure = { _ ->
                Napier.d("Failed to update messages")
            }
        )
    ),
    bookkeeper = provideBookkeeper(
        databaseHelper,
        MessageDto::class.simpleName.toString() + "UserList"
    ) { it }
)