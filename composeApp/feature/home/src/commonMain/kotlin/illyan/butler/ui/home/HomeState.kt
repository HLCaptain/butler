package illyan.butler.ui.home

import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse

data class HomeState(
    val isUserSignedIn: Boolean? = null,
    val isTutorialDone: Boolean? = null,
    val serverErrors: List<Pair<String, DomainErrorResponse>> = listOf(),
    val appErrors: List<DomainErrorEvent> = listOf(),
)
