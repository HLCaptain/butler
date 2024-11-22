package illyan.butler.ui.chat_layout

import illyan.butler.domain.model.DomainChat

data class ChatState(
    val chats: List<DomainChat> = emptyList()
)