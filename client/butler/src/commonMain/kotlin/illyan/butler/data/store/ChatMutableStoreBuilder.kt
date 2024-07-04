package illyan.butler.data.store

import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.Chat
import illyan.butler.domain.model.DomainChat
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
class ChatMutableStoreBuilder(
    databaseHelper: DatabaseHelper,
    chatNetworkDataSource: ChatNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideChatMutableStore(databaseHelper, chatNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideChatMutableStore(
    databaseHelper: DatabaseHelper,
    chatNetworkDataSource: ChatNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        Napier.d("Fetching chat $key")
        combine(
            flow { emit(chatNetworkDataSource.fetch()) },
            flow { emit(emptyList<ChatDto>()); emitAll(chatNetworkDataSource.fetchNewChats()) }
        ) { userChats, newChats ->
            Napier.d("Fetched chat ${(userChats + newChats).distinct()}")
            (userChats + newChats).distinctBy { it.id }.firstOrNull { it.id == key }
        }.filterNotNull()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            databaseHelper.queryAsOneOrNullFlow {
                Napier.d("Reading chat at $key")
                it.chatQueries.select(key)
            }.map { it?.toDomainModel() }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                Napier.d("Writing chat at $key with $local")
                db.chatQueries.upsert(local)
            }
        },
        delete = { key ->
            databaseHelper.withDatabase {
                Napier.d("Deleting chat at $key")
                it.chatQueries.delete(key)
            }
            chatNetworkDataSource.delete(key)
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all chats")
                it.chatQueries.deleteAll()
            }
        }
    ),
    converter = Converter.Builder<ChatDto, Chat, DomainChat>()
        .fromOutputToLocal { it.toLocalModel() }
        .fromNetworkToLocal { it.toLocalModel() }
        .build(),
).build(
    updater = Updater.by(
        post = { key, output ->
            val response = chatNetworkDataSource.upsert(output.toNetworkModel()).toDomainModel()
            UpdaterResult.Success.Typed(response)
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
        dataHistoryLocalDataSource,
        DomainChat::class.simpleName.toString()
    ) { it }
)