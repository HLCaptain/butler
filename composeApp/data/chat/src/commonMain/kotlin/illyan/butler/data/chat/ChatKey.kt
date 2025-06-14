package illyan.butler.data.chat

import illyan.butler.domain.model.Chat
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed class ChatKey {
    sealed class Read : ChatKey() {
        data class ByChatId(val chatId: Uuid) : Read()
        data class ByOwnerId(val ownerId: Uuid) : Read()
    }

    sealed class Write : ChatKey() {
        data object Create : Write()
        data object Upsert : Write()
    }

    data class Delete(val chat: Chat) : ChatKey()
}
