package illyan.butler.ui.home

import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.domain.model.Permission
import illyan.butler.domain.model.PermissionStatus

data class HomeScreenState(
    val isUserSignedIn: Boolean? = null,
    val isTutorialDone: Boolean? = null,
    val serverErrors: List<Pair<String, DomainErrorResponse>> = listOf(),
    val appErrors: List<DomainErrorEvent> = listOf(),
    val permissionStatuses: Map<Permission, PermissionStatus?> = mapOf(),
)
