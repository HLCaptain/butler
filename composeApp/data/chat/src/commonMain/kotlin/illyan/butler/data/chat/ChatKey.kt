package illyan.butler.data.chat

import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.Source
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed interface ChatKey {
    sealed interface Read : ChatKey {
        val source: Source
        data class ByChatId(override val source: Source, val chatId: Uuid) : Read
        data class BySource(override val source: Source) : Read
    }

    sealed interface Write : ChatKey {
        data object Create : Write
        data object Upsert : Write
    }

    data class Delete(val chat: Chat) : ChatKey
}
