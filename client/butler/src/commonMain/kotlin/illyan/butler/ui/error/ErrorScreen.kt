package illyan.butler.ui.error

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.ui.components.ButlerErrorDialogContent
import illyan.butler.ui.components.smallDialogWidth

@Composable
fun ErrorScreen(
    cleanError: (String) -> Unit,
    appErrors: List<DomainErrorEvent>,
    serverErrors: List<Pair<String, DomainErrorResponse>>
) {
    Crossfade(
        modifier = Modifier.animateContentSize(spring()),
        targetState = appErrors + serverErrors
    ) { _ ->
        val latestAppError = appErrors.maxByOrNull { it.timestamp }
        val latestServerError = serverErrors.maxByOrNull { it.second.timestamp }
        if (latestAppError != null && latestServerError != null) {
            if (latestAppError.timestamp > latestServerError.second.timestamp) {
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
fun AppErrorContent(
    appErrors: List<DomainErrorEvent>,
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
fun ServerErrorContent(
    serverErrors: List<Pair<String, DomainErrorResponse>>,
    clearError: (String) -> Unit
) {
    serverErrors.maxByOrNull { it.second.timestamp }?.let {
        ButlerErrorDialogContent(
            errorResponse = it.second,
            onClose = { clearError(it.first) },
            text = { Text(it.second.message ?: it.second.httpStatusCode.description) }
        )
    }
}