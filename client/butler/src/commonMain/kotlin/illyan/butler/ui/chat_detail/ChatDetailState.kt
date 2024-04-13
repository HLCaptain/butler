package illyan.butler.ui.chat_detail

import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage

data class ChatDetailState(
    val chat: DomainChat? = null,
    val messages: List<DomainMessage>? = null,
    val userId: String? = null
)
