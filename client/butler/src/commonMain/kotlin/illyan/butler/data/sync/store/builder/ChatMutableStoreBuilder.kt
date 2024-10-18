package illyan.butler.data.sync.store.builder

import illyan.butler.data.local.datasource.ChatLocalDataSource
import illyan.butler.data.local.datasource.DataHistoryLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toNetworkModel
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.sync.store.key.ChatKey
import illyan.butler.data.sync.store.provideBookkeeper
import illyan.butler.domain.model.DomainChat
import illyan.butler.utils.randomUUID
import kotlinx.coroutines.flow.map
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
        chatNetworkDataSource.fetchByChatId(key.chatId).map { it.toDomainModel() }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key ->
            require(key is ChatKey.Read.ByChatId)
            chatLocalDataSource.getChat(key.chatId)
        },
        writer = { key, local ->
            when (key) {
                is ChatKey.Write.Create -> chatLocalDataSource.upsertChat(local.copy(id = randomUUID()))
                is ChatKey.Write.Upsert -> chatLocalDataSource.upsertChat(local)
                is ChatKey.Read.ByChatId -> chatLocalDataSource.upsertChat(local) // From fetcher
                else -> throw IllegalArgumentException("Unsupported key type: ${key::class.qualifiedName}")
            }
        },
        delete = { key ->
            require(key is ChatKey.Delete.ByChatId)
            chatNetworkDataSource.delete(key.chatId)
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
            val chat = output.toNetworkModel()
            val newChat = when (key) {
                is ChatKey.Write.Create -> chatNetworkDataSource.upsert(chat.copy(id = null)).also {
                    chatLocalDataSource.replaceChat(it.id!!, it.toDomainModel())
                }
                is ChatKey.Write.Upsert -> chatNetworkDataSource.upsert(chat)
            }
            UpdaterResult.Success.Typed(newChat.toDomainModel())
        },
        onCompletion = null
    ),
    bookkeeper = provideBookkeeper(
        dataHistoryLocalDataSource,
        DomainChat::class.qualifiedName.toString()
    ) { it.toString() }
)