package illyan.butler.ui.chat_details

import illyan.butler.domain.model.DomainChat

data class ChatDetailsScreenState(
    val chat: DomainChat? = null,
    val userId: String? = null
)