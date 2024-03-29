package illyan.butler.ui.home

import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse

data class HomeScreenState(
    val isUserSignedIn: Boolean? = null,
    val signedInUserUUID: String? = null,
    val isTutorialDone: Boolean = false,
    val serverErrors: List<Pair<String, DomainErrorResponse>> = listOf(),
    val appErrors: List<DomainErrorEvent> = listOf()
)
