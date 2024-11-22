package illyan.butler.data.message

import illyan.butler.domain.model.DomainMessage
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single
import org.mobilenativefoundation.store.core5.ExperimentalStoreApi
import org.mobilenativefoundation.store.store5.StoreReadRequest
import org.mobilenativefoundation.store.store5.StoreReadResponse
import org.mobilenativefoundation.store.store5.StoreWriteRequest
import org.mobilenativefoundation.store.store5.StoreWriteResponse

@Single
class MessageStoreRepository(
    messageMutableStoreBuilder: MessageMutableStoreBuilder,
    chatMessageStoreBuilder: ChatMessageStoreBuilder,
    userMessageStoreBuilder: UserMessageStoreBuilder
) : MessageRepository {
    @OptIn(ExperimentalStoreApi::class)
    val messageMutableStore = messageMutableStoreBuilder.store
    val chatMessageMutableStore = chatMessageStoreBuilder.store
    private val userMessageStore = userMessageStoreBuilder.store

    @OptIn(ExperimentalStoreApi::class)
    override fun getMessageFlow(messageId: String): Flow<Pair<DomainMessage?, Boolean>> {
        return messageMutableStore.stream<StoreReadResponse<DomainMessage>>(
            StoreReadRequest.cached(MessageKey.Read.ByMessageId(messageId), true)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Message: $data")
            data to (it is StoreReadResponse.Loading)
        }
    }

    override fun getChatMessagesFlow(chatId: String): Flow<Pair<List<DomainMessage>?, Boolean>> {
        return chatMessageMutableStore.stream(
            StoreReadRequest.cached(MessageKey.Read.ByChatId(chatId), true)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Last 5 messages: ${data?.map { message -> message.id }?.takeLast(5)}")
            data to (it is StoreReadResponse.Loading)
        }
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun upsert(message: DomainMessage): String {
        return (messageMutableStore.write(
            StoreWriteRequest.of(
                key = if (message.id == null) MessageKey.Write.Create else MessageKey.Write.Upsert,
                value = message,
            )
        ) as? StoreWriteResponse.Success.Typed<DomainMessage>)?.value?.id!!
    }

    override fun getUserMessagesFlow(userId: String): Flow<Pair<List<DomainMessage>?, Boolean>> {
        return userMessageStore.stream(
            StoreReadRequest.cached(MessageKey.Read.ByUserId(userId), true)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Last 5 messages: ${data?.map { message -> message.id }?.takeLast(5)}")
            data to (it is StoreReadResponse.Loading)
        }
    }
}