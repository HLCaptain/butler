package illyan.butler.data.error

import illyan.butler.core.utils.getOsName
import illyan.butler.core.utils.getPlatformName
import illyan.butler.core.utils.getSystemMetadata
import illyan.butler.domain.model.DomainError
import illyan.butler.domain.model.ErrorCode
import illyan.butler.domain.model.ErrorState
import illyan.butler.shared.model.response.ServerErrorResponse
import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentLength
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Single
class ErrorMemoryRepository : ErrorRepository {
    private val _errorEventFlow = MutableSharedFlow<DomainError>()
    override val errorEventFlow: SharedFlow<DomainError> = _errorEventFlow.asSharedFlow()

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun reportError(throwable: Throwable) {
        Napier.e(throwable) { "Default throwable error reported" }
        val newEvent = DomainError.Event.Rich(
            id = Uuid.random(),
            platform = getPlatformName(),
            exception = throwable.toString().split(":").first(),
            message = throwable.message ?: "",
            stackTrace = throwable.stackTraceToString(),
            os = getOsName(),
            metadata = getSystemMetadata(),
            timestamp = System.currentTimeMillis(),
            state = ErrorState.NEW
        )
        _errorEventFlow.emit(newEvent)
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun reportError(response: HttpResponse) {
        try {
            val containsBody = (response.contentLength()?.toInt() ?: 0) > 0
            if (containsBody) {
                val serverResponse = response.body<ServerErrorResponse>()
                Napier.e { "Custom server error reported: ${serverResponse.statusCodes}" }
                serverResponse.statusCodes.forEach {
                    val domainResponse = DomainError.Response(
                        id = Uuid.random(),
                        httpStatusCode = response.status.value,
                        customErrorCode = it.code,
                        timestamp = response.responseTime.timestamp,
                        message = it.message
                    )
                    _errorEventFlow.emit(domainResponse)
                }
            } else {
                Napier.e { "Default server error reported" }
                val domainResponse = DomainError.Response(
                    id = Uuid.random(),
                    httpStatusCode = response.status.value,
                    customErrorCode = null,
                    timestamp = response.responseTime.timestamp
                )
                _errorEventFlow.emit(domainResponse)
            }
        } catch (t: Throwable) {
            Napier.e { "Default server error reported" }
            val domainResponse = DomainError.Response(
                id = Uuid.random(),
                httpStatusCode = response.status.value,
                customErrorCode = null,
                timestamp = response.responseTime.timestamp
            )
            _errorEventFlow.emit(domainResponse)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun reportSimpleError(code: ErrorCode) {
        _errorEventFlow.emit(
            DomainError.Event.Simple(
                id = Uuid.random(),
                code = code,
                timestamp = System.currentTimeMillis(),
            )
        )
    }
}
