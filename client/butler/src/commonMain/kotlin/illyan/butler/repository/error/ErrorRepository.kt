package illyan.butler.repository.error

import illyan.butler.model.DomainErrorEvent
import illyan.butler.model.DomainErrorResponse
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.SharedFlow

interface ErrorRepository {
    val appErrorEventFlow: SharedFlow<DomainErrorEvent>
    val serverErrorEventFlow: SharedFlow<DomainErrorResponse>

    suspend fun reportError(throwable: Throwable)
    suspend fun reportError(response: HttpResponse)
}
