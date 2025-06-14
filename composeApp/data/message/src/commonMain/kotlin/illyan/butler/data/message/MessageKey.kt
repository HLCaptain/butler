package illyan.butler.data.message

import illyan.butler.domain.model.Message
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed class MessageKey {
    sealed class Read : MessageKey() {
        data class ByMessageId(val messageId: Uuid) : Read()
        data class ByChatId(val chatId: Uuid) : Read()
    }

    sealed class Write : MessageKey() {
        data object Create : Write()
        data object Upsert : Write()
    }

    data class Delete(val message: Message) : MessageKey()
}
