package illyan.butler.data.chat

import illyan.butler.domain.model.DomainChat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse

@OptIn(ExperimentalStoreApi::class)
@Single
class ChatStoreRepository(
    chatMutableStoreBuilder: ChatMutableStoreBuilder,
    userChatStoreBuilder: UserChatStoreBuilder
) : ChatRepository {
    @OptIn(ExperimentalStoreApi::class)
    val chatMutableStore = chatMutableStoreBuilder.store

    val userChatStore = userChatStoreBuilder.store
    override suspend fun deleteAllChats(userId: String) {
        userChatStore.clear(ChatKey.Delete.ByUserId(userId))
    }

    override fun getChatFlow(chatId: String): Flow<Pair<DomainChat?, Boolean>> {
        return chatMutableStore.stream<StoreReadResponse<DomainChat>>(
            StoreReadRequest.cached(ChatKey.Read.ByChatId(chatId), true)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Chat is $data")
            data to (it is StoreReadResponse.Loading)
        }
    }

    override fun getUserChatsFlow(userId: String): Flow<Pair<List<DomainChat>?, Boolean>> {
        return userChatStore.stream(
            StoreReadRequest.cached(ChatKey.Read.ByUserId(userId), true)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Chats are $data")
            data to (it is StoreReadResponse.Loading)
        }
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun upsert(chat: DomainChat): String {
        val writtenChat = chatMutableStore.write(
            StoreWriteRequest.of(
                key = if (chat.id == null) ChatKey.Write.Create else ChatKey.Write.Upsert,
                value = chat,
            )
        )
        Napier.d("Chat upserted: $writtenChat")
        if (writtenChat is StoreWriteResponse.Success.Typed<*>) {
            val domainChat = (writtenChat as? StoreWriteResponse.Success.Typed<DomainChat>)?.value
            return domainChat?.id!!
        } else {
            throw IllegalStateException("Chat upsert failed")
        }
    }

    override suspend fun deleteChat(chatId: String) {
        chatMutableStore.clear(ChatKey.Delete.ByChatId(chatId))
    }
}
