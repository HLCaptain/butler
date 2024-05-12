package illyan.butler.ui.home

import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.domain.model.Permission

data class HomeScreenState(
    val isUserSignedIn: Boolean? = null,
    val signedInUserUUID: String? = null,
    val isTutorialDone: Boolean? = null,
    val serverErrors: List<Pair<String, DomainErrorResponse>> = listOf(),
    val appErrors: List<DomainErrorEvent> = listOf(),
    val preparedPermissionsToRequest: Set<Permission> = setOf(),
)
