package illyan.butler.ui.error

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import illyan.butler.domain.model.DomainError
import io.ktor.http.HttpStatusCode

@Composable
fun ErrorDialogContent(
    cleanError: (String) -> Unit,
    errors: List<DomainError>,
) {
    Crossfade(
        modifier = Modifier.animateContentSize(spring()),
        targetState = errors.filter { it !is DomainError.Event.Simple }
    ) { errors ->
        val appErrors = errors.mapNotNull { it as? DomainError.Event.Rich }
        val serverErrors = errors.mapNotNull { it as? DomainError.Response }
        val latestAppError = appErrors.maxByOrNull { it.timestamp }
        val latestServerError = serverErrors.maxByOrNull { it.timestamp }
        if (latestAppError != null && latestServerError != null) {
            if (latestAppError.timestamp > latestServerError.timestamp) {
                AppErrorContent(appErrors, cleanError)
            } else {
                ServerErrorContent(serverErrors, cleanError)
            }
        } else if (latestAppError != null) {
            AppErrorContent(appErrors, cleanError)
        } else if (latestServerError != null) {
            ServerErrorContent(serverErrors, cleanError)
        }
    }
}

@Composable
private fun AppErrorContent(
    appErrors: List<DomainError.Event.Rich>,
    clearError: (String) -> Unit
) {
    appErrors.maxByOrNull { it.timestamp }?.let {
        ButlerErrorDialogContent(
            errorEvent = it,
            onClose = { clearError(it.id) }
        )
    }
}

@Composable
private fun ServerErrorContent(
    serverErrors: List<DomainError.Response>,
    clearError: (String) -> Unit
) {
    serverErrors.maxByOrNull { it.timestamp }?.let {
        ButlerErrorDialogContent(
            errorResponse = it,
            onClose = { clearError(it.id) },
            text = { Text(it.message ?: HttpStatusCode.fromValue(it.httpStatusCode).description) }
        )
    }
}