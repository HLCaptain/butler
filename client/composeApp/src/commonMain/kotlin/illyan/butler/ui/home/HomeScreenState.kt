package illyan.butler.ui.home

import illyan.butler.model.DomainErrorEvent
import illyan.butler.model.DomainErrorResponse
import illyan.butler.model.Permission

data class HomeScreenState(
    val isUserSignedIn: Boolean? = null,
    val isTutorialDone: Boolean? = null,
    val serverErrors: List<Pair<String, DomainErrorResponse>> = listOf(),
    val appErrors: List<DomainErrorEvent> = listOf(),
    val preparedPermissionsToRequest: Set<Permission> = setOf(),
)
