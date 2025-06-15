package illyan.butler.data.chat

import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class)
@Single
class ChatStoreRepository(
    chatMutableStoreBuilder: ChatMutableStoreBuilder,
    userChatStoreBuilder: UserChatStoreBuilder,
) : ChatRepository {
    @OptIn(ExperimentalStoreApi::class)
    val chatMutableStore = chatMutableStoreBuilder.store

    val userChatStore = userChatStoreBuilder.store

    override fun getChatFlow(chatId: Uuid, source: Source): Flow<Chat?> {
        return chatMutableStore.stream<StoreReadResponse<Chat>>(
            StoreReadRequest.cached(ChatKey.Read.ByChatId(source, chatId), source is Source.Server)
        ).map {
            it.throwIfError()
            Napier.d("getChatFlow Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Chat is $data")
            data
        }
    }

    override fun getChatFlowBySource(source: Source): Flow<List<Chat>?> {
        return userChatStore.stream(
            StoreReadRequest.cached(
                ChatKey.Read.BySource(source),
                source is Source.Server
            )
        ).map {
            it.throwIfError()
            Napier.d("getChatFlowBySource Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Chat is $data")
            data
        }
    }

    @OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun create(chat: Chat): Uuid {
        val writtenChat = chatMutableStore.write(
            StoreWriteRequest.of(
                key = ChatKey.Write.Create,
                value = chat
            )
        )
        Napier.d("Chat upserted: $writtenChat")
        if (writtenChat is StoreWriteResponse.Success.Typed<*>) {
            val domainChat = (writtenChat as? StoreWriteResponse.Success.Typed<Chat>)?.value
            return domainChat?.id!!
        } else {
            throw IllegalStateException("Chat upsert failed")
        }
    }

    @OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class, ExperimentalTime::class)
    override suspend fun upsert(chat: Chat): Uuid {
        val writtenChat = chatMutableStore.write(
            StoreWriteRequest.of(
                key = ChatKey.Write.Upsert,
                value = chat
            )
        )
        Napier.d("Chat upserted: $writtenChat")
        if (writtenChat is StoreWriteResponse.Success.Typed<*>) {
            val domainChat = (writtenChat as? StoreWriteResponse.Success.Typed<Chat>)?.value
            return domainChat?.id!!
        } else {
            throw IllegalStateException("Chat upsert failed")
        }
    }

    override suspend fun deleteChat(chat: Chat) {
        chatMutableStore.clear(ChatKey.Delete(chat))
    }
}
