package illyan.butler.data.chat

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.network.datasource.ChatNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.core.sync.provideBookkeeper
import illyan.butler.domain.model.Chat
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.Updater
import org.mobilenativefoundation.store.store5.UpdaterResult
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@Single
class ChatMutableStoreBuilder(
    chatLocalDataSource: ChatLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) {
    @OptIn(ExperimentalStoreApi::class)
    val store = provideChatMutableStore(chatLocalDataSource, chatNetworkDataSource, dataHistoryLocalDataSource)
}

@OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
fun provideChatMutableStore(
    chatLocalDataSource: ChatLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
    dataHistoryLocalDataSource: DataHistoryLocalDataSource
) = MutableStoreBuilder.from(
    fetcher = Fetcher.ofFlow<ChatKey, Chat> { key ->
        require(key is ChatKey.Read.ByChatId)
        chatNetworkDataSource.fetchByChatId(key.chatId)
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is ChatKey.Read.ByChatId)
            chatLocalDataSource.getChat(key.chatId)
        },
        writer = { key, local ->
            when (key) {
                ChatKey.Write.Create, ChatKey.Write.Upsert -> chatLocalDataSource.upsertChat(local)
                is ChatKey.Read.ByChatId -> chatLocalDataSource.upsertChat(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key mimeType: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is ChatKey.Delete)
            if (!key.chat.deviceOnly) {
                chatNetworkDataSource.delete(key.chat.id)
            }
            chatLocalDataSource.deleteChatById(key.chat.id)
        },
        deleteAll = {
            chatLocalDataSource.deleteAllChats()
        }
    ),
    converter = NoopConverter()
).build(
    updater = Updater.by(
        post = { key, output ->
            require(key is ChatKey.Write)
            val newChat = when (key) {
                is ChatKey.Write.Create -> chatNetworkDataSource.upsert(output).also {
                    chatLocalDataSource.replaceChat(output.id, it)
                }
                is ChatKey.Write.Upsert -> if (output.deviceOnly) {
                    output // Do not upload device-only
                } else {
                    chatNetworkDataSource.upsert(output)
                }
            }
            UpdaterResult.Success.Typed(newChat)
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        Chat::class.qualifiedName.toString()
    ) { it.toString() }
)
