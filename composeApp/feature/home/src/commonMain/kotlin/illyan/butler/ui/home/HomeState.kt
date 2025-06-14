package illyan.butler.ui.home

import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.Chat
import illyan.butler.domain.model.DomainError

data class HomeState(
    val signedInUserId: String? = null,
    val clientId: String? = null,
    val errors: List<DomainError> = listOf(),
    val userChats: List<Chat> = listOf(),
    val deviceChats: List<Chat> = listOf(),
    val localChats: List<Chat> = listOf(),
    val credentials: List<ApiKeyCredential> = listOf(),
    val lastInteractionTimestampForChat: Map<String, Long?> = mapOf()
)
