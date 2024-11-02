package illyan.butler.model

import io.ktor.http.HttpStatusCode

data class DomainErrorResponse(
    val customErrorCode: Int?,
    val httpStatusCode: HttpStatusCode,
    val timestamp: Long,
    val message: String? = null,
)
