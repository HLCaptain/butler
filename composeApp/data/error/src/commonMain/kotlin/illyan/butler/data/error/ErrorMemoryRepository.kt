package illyan.butler.data.error

import illyan.butler.core.utils.getOsName
import illyan.butler.core.utils.getPlatformName
import illyan.butler.core.utils.getSystemMetadata
import illyan.butler.core.utils.randomUUID
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
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

@Single
class ErrorMemoryRepository : ErrorRepository {
    private val _appErrorEventFlow = MutableSharedFlow<DomainErrorEvent>()
    override val appErrorEventFlow: SharedFlow<DomainErrorEvent> = _appErrorEventFlow.asSharedFlow()

    private val _serverErrorEventFlow = MutableSharedFlow<DomainErrorResponse>()
    override val serverErrorEventFlow: SharedFlow<DomainErrorResponse> = _serverErrorEventFlow.asSharedFlow()

    override suspend fun reportError(throwable: Throwable) {
        Napier.e(throwable) { "Default throwable error reported" }
        val newErrorEvent = DomainErrorEvent(
            id = randomUUID(),
            platform = getPlatformName(),
            exception = throwable.toString().split(":").first(),
            message = throwable.message ?: "",
            stackTrace = throwable.stackTraceToString(),
            os = getOsName(),
            metadata = getSystemMetadata(),
            timestamp = System.currentTimeMillis(),
            state = ErrorState.NEW
        )
        _appErrorEventFlow.emit(newErrorEvent)
    }

    override suspend fun reportError(response: HttpResponse) {
        try {
            val containsBody = (response.contentLength()?.toInt() ?: 0) > 0
            if (containsBody) {
                val serverResponse = response.body<ServerErrorResponse>()
                Napier.e { "Custom server error reported: ${serverResponse.statusCodes}" }
                serverResponse.statusCodes.forEach {
                    val domainResponse = DomainErrorResponse(
                        httpStatusCode = response.status.value,
                        customErrorCode = it.code,
                        timestamp = response.responseTime.timestamp,
                        message = it.message
                    )
                    _serverErrorEventFlow.emit(domainResponse)
                }
            } else {
                Napier.e { "Default server error reported" }
                val domainResponse = DomainErrorResponse(
                    httpStatusCode = response.status.value,
                    customErrorCode = null,
                    timestamp = response.responseTime.timestamp
                )
                _serverErrorEventFlow.emit(domainResponse)
            }
        } catch (t: Throwable) {
            Napier.e { "Default server error reported" }
            val domainResponse = DomainErrorResponse(
                httpStatusCode = response.status.value,
                customErrorCode = null,
                timestamp = response.responseTime.timestamp
            )
            _serverErrorEventFlow.emit(domainResponse)
        }
    }
}