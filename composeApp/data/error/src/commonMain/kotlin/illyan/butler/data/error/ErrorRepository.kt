package illyan.butler.data.error

import illyan.butler.domain.model.DomainError
import illyan.butler.domain.model.ErrorCode
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.flow.SharedFlow

interface ErrorRepository {
    val errorEventFlow: SharedFlow<DomainError>

    suspend fun reportError(throwable: Throwable)
    suspend fun reportError(response: HttpResponse)
    suspend fun reportSimpleError(code: ErrorCode)
}
