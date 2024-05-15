package illyan.butler.ui.error

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.ui.components.ButlerErrorDialogContent

class ErrorScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ErrorScreenModel>()
        val state by screenModel.state.collectAsState()
        ErrorScreen(screenModel, state.appErrors, state.serverErrors)
    }
}

@Composable
private fun ErrorScreen(
    screenModel: ErrorScreenModel,
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
                AppErrorContent(appErrors, screenModel::clearError)
            } else {
                ServerErrorContent(serverErrors, screenModel::clearError)
            }
        } else if (latestAppError != null) {
            AppErrorContent(appErrors, screenModel::clearError)
        } else if (latestServerError != null) {
            ServerErrorContent(serverErrors, screenModel::clearError)
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
            onClose = { clearError(it.first) }
        )
    }
}