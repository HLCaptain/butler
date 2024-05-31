package illyan.butler.ui.error

import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse

data class ErrorScreenState(
    val serverErrors: List<Pair<String, DomainErrorResponse>> = listOf(),
    val appErrors: List<DomainErrorEvent> = listOf(),
)
