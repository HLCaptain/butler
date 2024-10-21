package illyan.butler.ui.new_chat

import illyan.butler.model.DomainModel

data class NewChatState(
    val availableModels: Map<DomainModel, List<String>>? = null,
    val creatingChat: Boolean = false,
    val newChatId: String? = null
)