package illyan.butler.domain.model

sealed class DomainError {
    abstract val id: String
    sealed class Event : DomainError() {
        data class Rich(
            override val id: String,
            val platform: String,
            val exception: String,
            val message: String,
            val stackTrace: String,
            val metadata: Map<String, String>,
            val os: String,
            val timestamp: Long,
            val state: ErrorState
        ) : Event()

        data class Simple(
            override val id: String,
            val code: ErrorCode,
            val timestamp: Long,
        ) : Event()
    }

    data class Response(
        override val id: String,
        val customErrorCode: Int?,
        val httpStatusCode: Int,
        val timestamp: Long,
        val message: String? = null,
    ) : DomainError()
}

enum class ErrorCode {
    MessageResponseError
}
