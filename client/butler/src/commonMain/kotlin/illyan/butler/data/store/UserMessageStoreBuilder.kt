package illyan.butler.data.store

import illyan.butler.data.local.datasource.MessageLocalDataSource
import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.mapping.toLocalModel
import illyan.butler.data.network.datasource.MessageNetworkDataSource
import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.db.Message
import illyan.butler.domain.model.DomainMessage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.store5.Converter
import org.mobilenativefoundation.store.store5.Fetcher
import org.mobilenativefoundation.store.store5.SourceOfTruth
import org.mobilenativefoundation.store.store5.StoreBuilder

@Single
class UserMessageStoreBuilder(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
) {
    val store = provideUserMessageStore(messageLocalDataSource, messageNetworkDataSource)
}

fun provideUserMessageStore(
    messageLocalDataSource: MessageLocalDataSource,
    messageNetworkDataSource: MessageNetworkDataSource,
) = StoreBuilder.from(
    fetcher = Fetcher.ofFlow { key: String ->
        Napier.d("Fetching messages for user $key")
        combine(
            flow { emit(messageNetworkDataSource.fetchByUser()) },
            flow { emit(emptyList<MessageDto>()); emitAll(messageNetworkDataSource.fetchNewMessages()) }
        ) { messages, newMessages ->
            Napier.d("Fetched ${(messages + newMessages).distinctBy { it.id }.size} messages")
            (messages + newMessages).distinctBy { it.id }
        }
    },
    sourceOfTruth = SourceOfTruth.of(
        reader = { key: String ->
            messageLocalDataSource.getAccessibleMessagesForUser(key)
        },
        writer = { key, local ->
            Napier.d("Writing messages for user $key")
            messageLocalDataSource.upsertMessages(local.map { it.toDomainModel() })
        }
    ),
    converter = Converter.Builder<List<MessageDto>, List<Message>, List<DomainMessage>>()
        .fromOutputToLocal { messages -> messages.map { it.toLocalModel() } }
        .fromNetworkToLocal { messages -> messages.map { it.toLocalModel() } }
        .build(),
).build()