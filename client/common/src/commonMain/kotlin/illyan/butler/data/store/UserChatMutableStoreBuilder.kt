package illyan.butler.data.store

import illyan.butler.data.firestore.model.FirestoreChat
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.ChatNetworkDataSource
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.Chat
import illyan.butler.domain.model.DomainChat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.OnUpdaterCompletion
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult

@Single
class UserChatMutableStoreBuilder(
    databaseHelper: DatabaseHelper,
    chatNetworkDataSource: ChatNetworkDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideUserChatMutableStore(databaseHelper, chatNetworkDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideUserChatMutableStore(
    databaseHelper: DatabaseHelper,
    chatNetworkDataSource: ChatNetworkDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        Napier.d("Fetching chats for user $key")
        chatNetworkDataSource.fetchByUser(userUUID = key)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            databaseHelper.queryAsListFlow {
                Napier.d("Reading chats for user $key")
                it.chatQueries.getChatsForUser(key)
            }.map { chats ->
                chats.map { it.toDomainModel() }
            }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                local.forEach {
                    Napier.d("Writing chat for user $key with $local")
                    db.chatQueries.upsert(it)
                }
            }
        },
        delete = { key ->
            databaseHelper.withDatabase {
                Napier.d("Deleting chat for user $key")
                it.chatQueries.deleteAllChatsForUser(key)
            }
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all chats")
                it.chatQueries.deleteAll()
            }
        }
    ),
    converter = Converter.Builder<List<FirestoreChat>, List<Chat>, List<DomainChat>>()
        .fromOutputToLocal { chats -> chats.map { it.toLocalModel() } }
        .fromNetworkToLocal { chats -> chats.map { it.toLocalModel() } }
        .build(),
).build(
    updater = Updater.by(
        post = { key, output ->
            output.forEach { chatNetworkDataSource.upsert(it.toNetworkModel()) }
            UpdaterResult.Success.Typed(output)
        },
        onCompletion = OnUpdaterCompletion(
            onSuccess = { _ ->
                Napier.d("Successfully updated chats")
            },
            onFailure = { _ ->
                Napier.d("Failed to update chats")
            }
        )
    ),
    bookkeeper = provideBookkeeper(
        databaseHelper,
        DomainChat::class.simpleName.toString() + "UserList"
    ) { it }
)