package illyan.butler.data.message

import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.Source
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

    override fun getMessageFlow(messageId: Uuid, source: Source): Flow<Message?> {
        return messageMutableStore.stream<StoreReadResponse<Message>>(
            StoreReadRequest.cached(MessageKey.Read.ByMessageId(source, messageId), source is Source.Server)
        ).map {
            it.throwIfError()
            Napier.d("getMessageFlow Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Message: $data")
            data
        }
    }

    override fun getChatMessagesFlow(chatId: Uuid, source: Source): Flow<List<Message>> {
        return chatMessageMutableStore.stream(
            StoreReadRequest.cached(MessageKey.Read.ByChatId(source, chatId), source is Source.Server)
        ).map {
            it.throwIfError()
            Napier.d("getChatMessagesFlow Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Last 5 messages: ${data?.map { message -> message.id }?.takeLast(5)}")
            data
        }.filterNotNull()
    }

    override suspend fun create(message: Message): Uuid {
        return (messageMutableStore.write(
            StoreWriteRequest.of(
                key = MessageKey.Write.Create,
                value = message
            )
        ) as? StoreWriteResponse.Success.Typed<Message>)?.value?.id ?: throw IllegalStateException("Message creation failed")
    }

    override suspend fun upsert(message: Message): Uuid {
        return (messageMutableStore.write(
            StoreWriteRequest.of(
                key = MessageKey.Write.Upsert,
                value = message
            )
        ) as? StoreWriteResponse.Success.Typed<Message>)?.value?.id ?: throw IllegalStateException("Message upsert failed")
    }

    override fun getMessagesBySource(source: Source): Flow<List<Message>> {
        return userMessageStore.stream(
            StoreReadRequest.cached(MessageKey.Read.BySource(source), source is Source.Server)
        ).map {
            it.throwIfError()
            Napier.d("getMessagesBySource Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Last 5 messages: ${data?.map { message -> message.id }?.takeLast(5)}")
            data
        }.filterNotNull()
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun delete(message: Message) {
        messageMutableStore.clear(MessageKey.Delete(message))
    }
}
