package illyan.butler.ui.chat_details

import illyan.butler.domain.model.Chat
import illyan.butler.domain.model.ModelConfig

data class ChatDetailsState(
    val chat: Chat? = null,
    val alternativeModels: List<ModelConfig> = emptyList()
)
