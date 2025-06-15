package illyan.butler.ui.chat_details

import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.AiSource

data class ChatDetailsState(
    val chat: Chat? = null,
    val alternativeModels: List<AiSource> = emptyList()
)
