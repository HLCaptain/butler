package illyan.butler.ui.new_chat

import illyan.butler.domain.model.DomainModel

data class NewChatState(
    val availableModels: List<DomainModel>? = null,
    val creatingChat: Boolean = false,
    val newChatId: String? = null
)