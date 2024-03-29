package illyan.butler.repository

import illyan.butler.data.mapping.toDomainModel
import illyan.butler.data.sqldelight.DatabaseHelper
import illyan.butler.db.ErrorEvent
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.domain.model.ErrorState
import illyan.butler.getOsName
import illyan.butler.getPlatformName
import illyan.butler.getSystemMetadata
import illyan.butler.util.log.randomUUID
import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.koin.core.annotation.Single

@Single
class ErrorRepository(
    private val databaseHelper: DatabaseHelper
) {
    private val _newErrorEventFlow = MutableSharedFlow<DomainErrorEvent>()
    val appErrorEventFlow = _newErrorEventFlow.asSharedFlow()

    private val _serverErrorEventFlow = MutableSharedFlow<DomainErrorResponse>()
    val serverErrorEventFlow = _serverErrorEventFlow.asSharedFlow()

    suspend fun reportError(throwable: Throwable) {
        Napier.e { "Error reported" }
        val localErrorEvent = ErrorEvent(
            id = randomUUID(),
            platform = getPlatformName(),
            message = throwable.message ?: "",
            stackTrace = throwable.stackTraceToString(),
            os = getOsName(),
            metadata = getSystemMetadata(),
            timestamp = System.currentTimeMillis(),
            state = ErrorState.NEW
        )
        val newErrorEvent = localErrorEvent.toDomainModel()
        _newErrorEventFlow.emit(newErrorEvent)
        databaseHelper.withDatabase { database ->
            database.errorEventQueries.upsert(localErrorEvent)
        }
    }

    suspend fun reportError(throwable: Throwable, response: HttpResponse) {
        reportError(throwable)
        Napier.e { "Server error reported" }
        val errorResponse = DomainErrorResponse(
            httpStatusCode = response.status,
            customErrorCode = response.body(),
            timestamp = response.responseTime.timestamp
        )
        _serverErrorEventFlow.emit(errorResponse)
    }
}