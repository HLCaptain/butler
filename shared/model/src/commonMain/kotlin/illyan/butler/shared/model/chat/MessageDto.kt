package illyan.butler.shared.model.chat

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Serializable
data class MessageDto(
    val id: Uuid,
    val sender: SenderType,
    /**
     * For a message there always can be text based content.
     */
    val content: String? = null,
    /**
     * To include pictures, media, etc. for a message.
     */
    val resourceIds: List<Uuid> = emptyList(),
    val time: Long? = null, // Unix timestamp
    val chatId: Uuid,
    val status: MessageStatus = MessageStatus.SENT,
) {
    @OptIn(ExperimentalUuidApi::class)
    val senderId = when (sender) {
        is SenderType.Ai -> sender.source.modelId
        is SenderType.User -> when (sender.source) {
            is Source.Device -> sender.source.deviceId
            is Source.Server -> sender.source.userId
        }.toString()
        is SenderType.System -> "system"
    }
}
