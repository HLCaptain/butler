package illyan.butler.data.store

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.Chat
import illyan.butler.db.ChatMember
import illyan.butler.domain.model.DomainChat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class UserChatMutableStoreBuilder(
    databaseHelper: DatabaseHelper,
    chatNetworkDataSource: ChatNetworkDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideUserChatMutableStore(databaseHelper, chatNetworkDataSource)
}

@OptIn(ExperimentalStoreApi::class, ExperimentalCoroutinesApi::class)
fun provideUserChatMutableStore(
    databaseHelper: DatabaseHelper,
    chatNetworkDataSource: ChatNetworkDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.of { key ->
        Napier.d("Fetching chats for user $key")
        chatNetworkDataSource.fetch()
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            databaseHelper.queryAsListFlow {
                Napier.d("Reading chats for user $key")
                it.chatQueries.selectAll()
            }.map { chats ->
                Napier.v { "Chats: $chats" }
                chats // FIXME: all chat is exposed this way, think about security when we have more users on a single device, accessing each other's chats
                    .filter { it.members.contains(key) }
                    .map { it.toDomainModel() }
            }
        },
        writer = { key, local ->
            databaseHelper.withDatabase { db ->
                local.forEach { chat ->
                    Napier.d("Writing chat for user $key with $local")
                    val currentMembers = chat.members.map { ChatMember("${key};${chat.id}", it, chat.id) }
                    db.chatMemberQueries.deleteAllChatMembers(chat.id)
                    currentMembers.forEach { db.chatMemberQueries.upsert(it) }
                    db.chatQueries.upsert(chat)
                }
            }
        },
        delete = { key ->
            databaseHelper.withDatabase {
                Napier.d("Deleting chat for user $key")
                it.chatMemberQueries.selectAllUserChats(key).executeAsList().forEach { chat ->
                    it.chatMemberQueries.deleteAllChatMembers(chat.id)
                    it.chatQueries.delete(chat.id)
                }
            }
        },
        deleteAll = {
            databaseHelper.withDatabase {
                Napier.d("Deleting all chats")
                it.chatMemberQueries.deleteAll()
                it.chatQueries.deleteAll()
            }
        }
    ),
    converter = Converter.Builder<List<ChatDto>, List<Chat>, List<DomainChat>>()
        .fromOutputToLocal { chats -> chats.map { it.toLocalModel() } }
        .fromNetworkToLocal { chats -> chats.map { it.toLocalModel() } }
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
        databaseHelper,
        DomainChat::class.simpleName.toString() + "UserList"
    ) { it.toString() }
)