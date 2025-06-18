package illyan.butler.ui.chat_layout

import illyan.butler.domain.model.Chat

data class ChatState(
    val chats: List<Chat> = emptyList()
)