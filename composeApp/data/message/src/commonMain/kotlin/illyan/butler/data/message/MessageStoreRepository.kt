package illyan.butler.data.message

import illyan.butler.domain.model.Message
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class)
@Single
class MessageStoreRepository(
    messageMutableStoreBuilder: MessageMutableStoreBuilder,
    chatMessageStoreBuilder: ChatMessageStoreBuilder,
    userMessageStoreBuilder: UserMessageStoreBuilder
) : MessageRepository {

    val messageMutableStore = messageMutableStoreBuilder.store
    val chatMessageMutableStore = chatMessageStoreBuilder.store
    private val userMessageStore = userMessageStoreBuilder.store

    override fun getMessageFlow(messageId: Uuid): Flow<Message?> {
        return messageMutableStore.stream<StoreReadResponse<Message>>(
            StoreReadRequest.cached(MessageKey.Read.ByMessageId(messageId), true)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Message: $data")
            data
        }
    }

    override fun getChatMessagesFlow(chatId: Uuid): Flow<List<Message>> {
        return chatMessageMutableStore.stream(
            StoreReadRequest.cached(MessageKey.Read.ByChatId(chatId), !deviceOnly)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Last 5 messages: ${data?.map { message -> message.id }?.takeLast(5)}")
            data
        }.filterNotNull()
    }

    override fun getOwnerMessagesFlow(ownerId: Uuid): Flow<List<Message>> {
        TODO("Not yet implemented")
    }

    @OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class)
    override suspend fun upsert(message: Message): Uuid {
        return (messageMutableStore.write(
            StoreWriteRequest.of(
                key = if (message.id == null) MessageKey.Write.Create else MessageKey.Write.Upsert,
                value = message
            )
        ) as? StoreWriteResponse.Success.Typed<Message>)?.value?.id!!
    }

    override fun getUserMessagesFlow(ownerId: Uuid): Flow<List<Message>> {
        return userMessageStore.stream(
            StoreReadRequest.cached(MessageKey.Read.ByOwnerId(userId))
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Last 5 messages: ${data?.map { message -> message.id }?.takeLast(5)}")
            data
        }.filterNotNull()
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun delete(message: Message) {
        messageMutableStore.clear(MessageKey.Delete(message.id!!, message.chatId))
    }
}
