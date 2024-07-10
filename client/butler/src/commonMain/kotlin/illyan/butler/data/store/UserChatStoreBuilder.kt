package illyan.butler.data.store

import illyan.butler.data.local.datasource.ChatLocalDataSource
import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.network.datasource.ChatNetworkDataSource
import illyan.butler.data.network.model.chat.ChatDto
import illyan.butler.domain.model.DomainChat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder

@Single
class UserChatStoreBuilder(
    chatLocalDataSource: ChatLocalDataSource,
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
) {
    val store = provideUserChatStore(chatLocalDataSource, messageLocalDataSource, chatNetworkDataSource)
}

fun provideUserChatStore(
    chatLocalDataSource: ChatLocalDataSource,
    messageLocalDataSource: MessageLocalDataSource,
    chatNetworkDataSource: ChatNetworkDataSource,
) = StoreBuilder.from(
    fetcher = Fetcher.ofFlow { key ->
        Napier.d("Fetching chats for user $key")
        combine(
            flow { emit(chatNetworkDataSource.fetch()) },
            flow { emit(emptyList<ChatDto>()); emitAll(chatNetworkDataSource.fetchNewChats()) }
        ) { userChats, newChats ->
            Napier.d("Fetched ${(userChats + newChats).distinct().size} chats")
            val modifiedChats = newChats.filter { chat -> userChats.any { it.id == chat.id } }
            val chatsNeedingUpdate = (userChats + newChats).filter { chat -> modifiedChats.any { it.id != chat.id } } + modifiedChats
            chatsNeedingUpdate
                .filter { it.members.contains(key) }
                .also { chats ->
                    val newMessages = chats.flatMap { chat -> chat.lastFewMessages.map { it.toDomainModel() } }
                    messageLocalDataSource.upsertMessages(newMessages)
                }
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            Napier.d("Reading chats for user $key")
            combine(
                flow { emit(chatNetworkDataSource.fetch()) },
                flow { emit(emptyList<ChatDto>()); emitAll(chatNetworkDataSource.fetchNewChats()) },
                chatLocalDataSource.getChatsByUser(key).map { it ?: emptyList() }
            ) { userChats, newChats, localChats ->
                Napier.d("Fetched ${(userChats + newChats).distinctBy { it.id }.size} chats")
                val chatsNeedingUpdate = newChats.filterNot { chat -> localChats.contains(chat.toDomainModel()) }
                chatsNeedingUpdate
                    .filter { it.members.contains(key) }
                    .also { chats ->
                        val newMessages = chats.flatMap { chat -> chat.lastFewMessages.map { it.toDomainModel() } }
                        messageLocalDataSource.upsertMessages(newMessages)
                    }
                    .map { it.toDomainModel() }
                    .also { allChats ->
                        val updatedAndNewChats = allChats.filter { chat -> localChats.find { it.id == chat.id } != chat }
                        if (updatedAndNewChats.isNotEmpty()) chatLocalDataSource.upsertChats(updatedAndNewChats)
                    } + localChats.filterNot { chat -> chatsNeedingUpdate.any { chat.id == it.id } }
            }
        },
        writer = { key, local ->
            Napier.d("Writing chat for user $key with $local")
            chatLocalDataSource.upsertChats(local)
        },
    ),
    converter = Converter.Builder<List<ChatDto>, List<DomainChat>, List<DomainChat>>()
        .fromOutputToLocal { it }
        .fromNetworkToLocal { chats -> chats.map { it.toDomainModel() } }
        .build(),
).build()