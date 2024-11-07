package illyan.butler.domain.model

data class DomainErrorResponse(
    val customErrorCode: Int?,
    val httpStatusCode: Int,
    val timestamp: Long,
    val message: String? = null,
)
