package illyan.butler.ui.chat_detail

import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.domain.model.PermissionStatus

data class ChatDetailState(
    val chat: DomainChat? = null,
    val messages: List<DomainMessage>? = null,
    val userId: String? = null,
    val isRecording: Boolean = false,
    val canRecordAudio: Boolean = false,
    val sounds: Map<String, Float> = emptyMap(),
    val playingAudio: String? = null,
    val images: Map<String, ByteArray> = emptyMap(),
    val galleryPermission: PermissionStatus? = null
)
