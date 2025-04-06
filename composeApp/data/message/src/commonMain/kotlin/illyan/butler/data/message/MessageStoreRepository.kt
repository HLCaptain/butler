package illyan.butler.data.message

import illyan.butler.domain.model.DomainMessage
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
    override fun getMessageFlow(messageId: String, deviceOnly: Boolean): Flow<DomainMessage?> {
        return messageMutableStore.stream<StoreReadResponse<DomainMessage>>(
            StoreReadRequest.cached(MessageKey.Read.ByMessageId(messageId), !deviceOnly)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Message: $data")
            data
        }
    }

    override fun getChatMessagesFlow(chatId: String, deviceOnly: Boolean): Flow<List<DomainMessage>> {
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

    @OptIn(ExperimentalStoreApi::class, ExperimentalUuidApi::class)
    override suspend fun upsert(message: DomainMessage, deviceOnly: Boolean): String {
        return (messageMutableStore.write(
            StoreWriteRequest.of(
                key = if (deviceOnly) MessageKey.Write.DeviceOnly else if (message.id == null) MessageKey.Write.Create else MessageKey.Write.Upsert,
                value = message.copy(
                    id = message.id ?: Uuid.random().toString(), // ID cannot be null on write
                    time = if (deviceOnly) message.time ?: Clock.System.now().toEpochMilliseconds() else message.time
                ),
            )
        ) as? StoreWriteResponse.Success.Typed<DomainMessage>)?.value?.id!!
    }

    override fun getUserMessagesFlow(userId: String, deviceOnly: Boolean): Flow<List<DomainMessage>> {
        return userMessageStore.stream(
            StoreReadRequest.cached(MessageKey.Read.ByUserId(userId), !deviceOnly)
        ).map {
            it.throwIfError()
            Napier.d("Read Response: ${it::class.qualifiedName}")
            val data = it.dataOrNull()
            Napier.d("Last 5 messages: ${data?.map { message -> message.id }?.takeLast(5)}")
            data
        }.filterNotNull()
    }

    @OptIn(ExperimentalStoreApi::class)
    override suspend fun delete(message: DomainMessage, deviceOnly: Boolean) {
        messageMutableStore.clear(MessageKey.Delete(message.id!!, message.chatId, deviceOnly))
    }
}
