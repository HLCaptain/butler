package illyan.butler.data.store

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.MessageDto
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.Message
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import io.github.aakira.napier.Napier
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
        Napier.d("Fetching chat $key")
        messageNetworkDataSource.fetch(key)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            databaseHelper.queryAsOneOrNullFlow {
                Napier.d("Reading chat at $key")
                it.messageQueries.select(key)
            }.map { it?.toDomainModel() }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                Napier.d("Writing chat at $key with $local")
                db.messageQueries.upsert(local)
            }
        },
        delete = { key ->
            databaseHelper.withDatabase {
                Napier.d("Deleting chat at $key")
                it.messageQueries.delete(key)
            }
            messageNetworkDataSource.delete(key)
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all chats")
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
            messageNetworkDataSource.upsert(output.toNetworkModel())
            UpdaterResult.Success.Typed(output)
        },
        onCompletion = OnUpdaterCompletion(
            onSuccess = { _ ->
                Napier.d("Successfully updated chat")
            },
            onFailure = { _ ->
                Napier.d("Failed to update chat")
            }
        )
    ),
    bookkeeper = provideBookkeeper(
        databaseHelper,
        DomainChat::class.simpleName.toString()
    ) { it }
)