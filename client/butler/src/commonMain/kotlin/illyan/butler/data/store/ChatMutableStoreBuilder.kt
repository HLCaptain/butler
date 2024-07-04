package illyan.butler.data.store

import illyan.butler.data.local.datasource.ChatLocalDataSource
import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
import illyan.butler.domain.model.DomainChat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
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
class ChatMutableStoreBuilder(
    chatLocalDataSource: ChatLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideChatMutableStore(chatLocalDataSource, chatNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideChatMutableStore(
    chatLocalDataSource: ChatLocalDataSource,
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
            Napier.d("Reading chat at $key")
            chatLocalDataSource.getChat(key)
        },
        writer = { key, local ->
            Napier.d("Writing chat at $key with $local")
            chatLocalDataSource.upsertChat(local)
        },
        delete = { key ->
            Napier.d("Deleting chat at $key")
            chatLocalDataSource.deleteChatById(key)
            chatNetworkDataSource.delete(key)
        },
        deleteAll = {
            Napier.d("Deleting all chats")
            chatLocalDataSource.deleteAllChats()
        }
    ),
    converter = Converter.Builder<ChatDto, DomainChat, DomainChat>()
        .fromOutputToLocal { it }
        .fromNetworkToLocal { it.toDomainModel() }
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