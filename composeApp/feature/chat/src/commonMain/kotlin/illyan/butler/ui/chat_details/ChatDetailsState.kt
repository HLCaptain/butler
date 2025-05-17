package illyan.butler.ui.chat_details

import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.ModelConfig

data class ChatDetailsState(
    val chat: DomainChat? = null,
    val alternativeModels: List<ModelConfig> = emptyList()
)
