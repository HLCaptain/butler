package illyan.butler.ui.new_chat

import illyan.butler.domain.model.DomainModel

data class NewChatState(
    val userId: String? = null,
    val clientId: String? = null,
    val providerModels: List<DomainModel>? = null,
    val serverModels: List<DomainModel>? = null,
    val localModels: List<DomainModel>? = null,
    val creatingChat: Boolean = false,
    val newChatId: String? = null,
)