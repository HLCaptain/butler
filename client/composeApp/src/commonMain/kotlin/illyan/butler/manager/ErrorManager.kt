package illyan.butler.manager

import illyan.butler.repository.error.ErrorRepository
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

@Single
class ErrorManager(
    private val errorRepository: ErrorRepository
) {
    suspend fun reportError(throwable: Throwable) {
        errorRepository.reportError(throwable)
    }
    suspend fun reportError(response: HttpResponse) {
        errorRepository.reportError(response)
    }
    fun reportError(throwable: Throwable, coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            errorRepository.reportError(throwable)
        }
    }
    val appErrors = errorRepository.appErrorEventFlow
    val serverErrors = errorRepository.serverErrorEventFlow
}