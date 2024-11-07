package illyan.butler.error

import illyan.butler.data.error.ErrorRepository
import io.ktor.client.statement.HttpResponse
import org.koin.core.annotation.Single

@Single
class ErrorManager(
    private val errorRepository: ErrorRepository
) {
    suspend fun reportError(throwable: Throwable) = errorRepository.reportError(throwable)
    suspend fun reportError(response: HttpResponse) = errorRepository.reportError(response)
    val appErrors = errorRepository.appErrorEventFlow
    val serverErrors = errorRepository.serverErrorEventFlow
}