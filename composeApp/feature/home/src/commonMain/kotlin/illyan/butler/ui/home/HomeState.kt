package illyan.butler.ui.home

import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse

data class HomeState(
    val signedInUserId: String? = null,
    val clientId: String? = null,
    val serverErrors: List<Pair<String, DomainErrorResponse>> = listOf(),
    val appErrors: List<DomainErrorEvent> = listOf(),
    val userChats: List<DomainChat> = listOf(),
    val deviceChats: List<DomainChat> = listOf(),
    val localChats: List<DomainChat> = listOf(),
    val credentials: List<ApiKeyCredential> = listOf(),
)
