package illyan.butler.ui.chat_detail

import illyan.butler.domain.model.Chat
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.AiSource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class ChatDetailState @OptIn(ExperimentalUuidApi::class) constructor(
    val chat: Chat? = null,
    val messages: List<Message>? = null,
    val isRecording: Boolean = false,
    val sounds: Map<Uuid, Float> = emptyMap(),
    val playingAudio: Uuid? = null,
    val images: Map<Uuid, ByteArray> = emptyMap(),
    val selectedNewChatModel: AiSource? = null,
)
