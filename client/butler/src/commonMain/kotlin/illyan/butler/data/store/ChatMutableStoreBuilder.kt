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
class ChatMutableStoreBuilder(
    chatLocalDataSource: ChatLocalDataSource,
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideChatMutableStore(chatLocalDataSource, messageLocalDataSource, chatNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class)
fun provideChatMutableStore(
    chatLocalDataSource: ChatLocalDataSource,
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        Napier.d("Fetching chat $key")
        combine(
            flow { emit(chatNetworkDataSource.fetch()) },
            flow { emit(emptyList<ChatDto>()); emitAll(chatNetworkDataSource.fetchNewChats()) }
        ) { chats, newChats ->
            Napier.d("Fetched chat $key")
            val chat = chats.first { it.id == key }
            val newChat = newChats.find { it.id == key }
            newChat?.let { messageLocalDataSource.upsertMessages(newChat.lastFewMessages.map { it.toDomainModel() }) }
            newChat ?: chat
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            Napier.d("Reading chat at $key")
            combine(
                flow { emit(chatNetworkDataSource.fetch()) },
                flow { emit(emptyList<ChatDto>()); emitAll(chatNetworkDataSource.fetchNewChats()) },
                chatLocalDataSource.getChat(key)
            ) { chats, newChats, localChat ->
                Napier.d("Fetched chat $key")
                val chat = chats.first { it.id == key }
                val newChat = newChats.find { it.id == key }
                newChat?.let { messageLocalDataSource.upsertMessages(newChat.lastFewMessages.map { it.toDomainModel() }) }
                val freshChat = (newChat ?: chat).toDomainModel()
                if (localChat != freshChat) chatLocalDataSource.upsertChat(freshChat)
                freshChat
            }
        },
        writer = { key, local ->
            Napier.d("Writing chat at $key with $local")
            chatLocalDataSource.upsertChat(local)
        },
        delete = { key ->
            Napier.d("Deleting chat at $key")
            chatNetworkDataSource.delete(key)
            chatLocalDataSource.deleteChatById(key)
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
            val response = chatNetworkDataSource.upsert(output.toNetworkModel().copy(id = null)).toDomainModel()
            chatLocalDataSource.replaceChat(key, response)
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