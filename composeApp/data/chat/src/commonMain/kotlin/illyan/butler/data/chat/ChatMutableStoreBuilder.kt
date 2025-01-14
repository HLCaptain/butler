package illyan.butler.data.chat

import illyan.butler.core.local.datasource.ChatLocalDataSource
import illyan.butler.core.local.datasource.DataHistoryLocalDataSource
import illyan.butler.core.network.datasource.ChatNetworkDataSource
import illyan.butler.core.sync.NoopConverter
import illyan.butler.core.sync.provideBookkeeper
import illyan.butler.domain.model.DomainChat
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.MutableStoreBuilder
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
                ChatKey.Write.Create, ChatKey.Write.Upsert, ChatKey.Write.DeviceOnly -> chatLocalDataSource.upsertChat(local)
                is ChatKey.Read.ByChatId -> chatLocalDataSource.upsertChat(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key type: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is ChatKey.Delete.ByChatId)
            if (!key.deviceOnly) {
                chatNetworkDataSource.delete(key.chatId)
            }
            chatLocalDataSource.deleteChatById(key.chatId)
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
                is ChatKey.Write.Create -> chatNetworkDataSource.upsert(output.copy(id = null)).also {
                    chatLocalDataSource.replaceChat(output.id!!, it)
                }
                is ChatKey.Write.Upsert -> chatNetworkDataSource.upsert(output)
                is ChatKey.Write.DeviceOnly -> output // Do not upload device-only chats
            }
            UpdaterResult.Success.Typed(newChat)
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        DomainChat::class.qualifiedName.toString()
    ) { it.toString() }
)
