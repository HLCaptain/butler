package illyan.butler.data.chat

import illyan.butler.domain.model.DomainChat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalStoreApi::class)
@Single
class ChatStoreRepository(
    chatMutableStoreBuilder: ChatMutableStoreBuilder,
    userChatStoreBuilder: UserChatStoreBuilder
) : ChatRepository {
    @OptIn(ExperimentalStoreApi::class)
    val chatMutableStore = chatMutableStoreBuilder.store

    val userChatStore = userChatStoreBuilder.store
    override suspend fun deleteAllChats(userId: String, deviceOnly: Boolean) {
        userChatStore.clear(ChatKey.Delete.ByUserId(userId, deviceOnly))
    }

    override fun getChatFlow(chatId: String, deviceOnly: Boolean): Flow<DomainChat?> {
        return chatMutableStore.stream<StoreReadResponse<DomainChat>>(
            StoreReadRequest.cached(ChatKey.Read.ByChatId(chatId), !deviceOnly)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Chat is $data")
            data
        }
    }

    override fun getUserChatsFlow(userId: String, deviceOnly: Boolean): Flow<List<DomainChat>> {
        return userChatStore.stream(
            StoreReadRequest.cached(ChatKey.Read.ByUserId(userId), !deviceOnly)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Chats are $data")
            data
        }.filterNotNull()
    }

    @OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class)
    override suspend fun upsert(chat: DomainChat, deviceOnly: Boolean): String {
        val writtenChat = chatMutableStore.write(
            StoreWriteRequest.of(
                key = if (deviceOnly) ChatKey.Write.DeviceOnly else if (chat.id == null) ChatKey.Write.Create else ChatKey.Write.Upsert,
                value = chat.copy(
                    id = chat.id ?: Uuid.random().toString(), // ID cannot be null on write
                    created = if (deviceOnly) chat.created ?: Clock.System.now().toEpochMilliseconds() else chat.created
                ),
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

    override suspend fun deleteChat(chatId: String, deviceOnly: Boolean) {
        chatMutableStore.clear(ChatKey.Delete.ByChatId(chatId, deviceOnly))
    }
}
