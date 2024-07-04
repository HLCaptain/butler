package illyan.butler.data.store

import illyan.butler.data.local.datasource.ChatLocalDataSource
import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
import illyan.butler.domain.model.DomainChat
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
class UserChatMutableStoreBuilder(
    chatLocalDataSource: ChatLocalDataSource,
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideUserChatMutableStore(chatLocalDataSource, messageLocalDataSource, chatNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideUserChatMutableStore(
    chatLocalDataSource: ChatLocalDataSource,
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        Napier.d("Fetching chats for user $key")
        combine(
            flow { emit(chatNetworkDataSource.fetch()) },
            flow { emit(emptyList<ChatDto>()); emitAll(chatNetworkDataSource.fetchNewChats()) }
        ) { userChats, newChats ->
            Napier.d("Fetched ${(userChats + newChats).distinct().size} chats")

            (userChats + newChats)
                .filter { it.members.contains(key) }
                .distinctBy { it.id }
                .also { chats ->
                    val newMessages = chats.flatMap { chat -> chat.lastFewMessages.map { it.toDomainModel() } }
                    messageLocalDataSource.upsertMessages(newMessages)
                }
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            Napier.d("Reading chats for user $key")
            chatLocalDataSource.getChatsByUser(key)
        },
        writer = { key, local ->
            Napier.d("Writing chat for user $key with $local")
            chatLocalDataSource.upsertChats(local)
        },
        delete = { key ->
            Napier.d("Deleting chat for user $key")
            chatLocalDataSource.deleteChatByUserId(key)
        },
        deleteAll = {
            Napier.d("Deleting all chats")
            chatLocalDataSource.deleteAllChats()
        }
    ),
    converter = Converter.Builder<List<ChatDto>, List<DomainChat>, List<DomainChat>>()
        .fromOutputToLocal { it }
        .fromNetworkToLocal { chats -> chats.map { it.toDomainModel() } }
        .build(),
).build(
    updater = Updater.by(
        post = { key, output ->
            val response = output.map { chatNetworkDataSource.upsert(it.toNetworkModel()).toDomainModel() }
            UpdaterResult.Success.Typed(response)
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
        dataHistoryLocalDataSource,
        DomainChat::class.simpleName.toString() + "UserList"
    ) { it.toString() }
)