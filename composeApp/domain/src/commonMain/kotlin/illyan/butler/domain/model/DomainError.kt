package illyan.butler.domain.model

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
sealed class DomainError {
    abstract val id: Uuid
    sealed class Event : DomainError() {
        data class Rich(
            override val id: Uuid,
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
            override val id: Uuid,
            val code: ErrorCode,
            val timestamp: Long,
        ) : Event()
    }

    data class Response(
        override val id: Uuid,
        val customErrorCode: Int?,
        val httpStatusCode: Int,
        val timestamp: Long,
        val message: String? = null,
    ) : DomainError()
}

enum class ErrorCode {
    MessageResponseError,
    ChatRefreshError
}
