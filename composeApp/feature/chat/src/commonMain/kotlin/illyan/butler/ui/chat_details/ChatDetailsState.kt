package illyan.butler.ui.chat_details

import illyan.butler.domain.model.DomainChat

data class ChatDetailsState(
    val chat: DomainChat? = null,
    val alternativeModels: List<Pair<String, String>> = emptyList()
)
