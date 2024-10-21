package illyan.butler.ui.chat_layout

import illyan.butler.model.DomainChat

data class ChatState(
    val chats: List<DomainChat> = emptyList()
)