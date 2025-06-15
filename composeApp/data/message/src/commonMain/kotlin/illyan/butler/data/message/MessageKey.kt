package illyan.butler.data.message

import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.Source
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed interface MessageKey {
    sealed interface Read : MessageKey {
        val source: Source
        data class ByMessageId(override val source: Source, val messageId: Uuid) : Read
        data class ByChatId(override val source: Source, val chatId: Uuid) : Read
        data class BySource(override val source: Source) : Read
    }

    sealed interface Write : MessageKey {
        data object Create : Write
        data object Upsert : Write
    }

    data class Delete(val message: Message) : MessageKey
}
