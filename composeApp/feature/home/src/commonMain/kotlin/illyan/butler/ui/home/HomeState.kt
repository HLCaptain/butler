package illyan.butler.ui.home

import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainError

data class HomeState(
    val signedInUserId: String? = null,
    val clientId: String? = null,
    val errors: List<DomainError> = listOf(),
    val userChats: List<DomainChat> = listOf(),
    val deviceChats: List<DomainChat> = listOf(),
    val localChats: List<DomainChat> = listOf(),
    val credentials: List<ApiKeyCredential> = listOf(),
    val lastInteractionTimestampForChat: Map<String, Long?> = mapOf()
)
