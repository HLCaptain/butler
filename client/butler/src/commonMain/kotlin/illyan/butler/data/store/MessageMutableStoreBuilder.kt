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
import kotlinx.coroutines.flow.filterNotNull
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
class MessageMutableStoreBuilder(
    databaseHelper: DatabaseHelper,
    messageNetworkDataSource: MessageNetworkDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideMessageMutableStore(databaseHelper, messageNetworkDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideMessageMutableStore(
    databaseHelper: DatabaseHelper,
    messageNetworkDataSource: MessageNetworkDataSource
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
            databaseHelper.queryAsOneOrNullFlow {
                Napier.d("Reading message at $key")
                it.messageQueries.select(key)
            }.map { it?.toDomainModel() }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                Napier.d("Writing message at $key with $local")
                db.messageQueries.upsert(local)
            }
        },
        delete = { key ->
            var chatId = ""
            databaseHelper.withDatabase {
                Napier.d("Deleting message at $key")
                chatId = it.messageQueries.select(key).executeAsOne().chatId
                it.messageQueries.delete(key)
            }
            messageNetworkDataSource.delete(key, chatId)
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all messages")
                it.messageQueries.deleteAll()
            }
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
        databaseHelper,
        Message::class.simpleName.toString()
    ) { it }
)