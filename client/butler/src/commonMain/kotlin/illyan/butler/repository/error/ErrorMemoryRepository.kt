package illyan.butler.repository.error

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.db.ErrorEvent
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.domain.model.ErrorState
import illyan.butler.getOsName
import illyan.butler.getPlatformName
import illyan.butler.getSystemMetadata
import illyan.butler.utils.randomUUID
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
        Napier.e(throwable) { "Error reported" }
        val localErrorEvent = ErrorEvent(
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
        val newErrorEvent = localErrorEvent.toDomainModel()
        _appErrorEventFlow.emit(newErrorEvent)
    }

    override suspend fun reportError(response: HttpResponse) {
        val errorResponse = DomainErrorResponse(
            httpStatusCode = response.status,
            customErrorCode = if ((response.contentLength() ?: 0) > 0) response.body() else null, // Checking if anything is returned in the body
            timestamp = response.responseTime.timestamp
        )
        _serverErrorEventFlow.emit(errorResponse)
    }
}