package illyan.butler.ui.error

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import illyan.butler.ui.components.ButlerErrorDialogContent
import illyan.butler.ui.dialog.ButlerDialog

class ErrorScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ErrorScreenModel>()
        val state by screenModel.state.collectAsState()
        val numberOfErrors = state.appErrors.size + state.serverErrors.size
        ErrorScreen(state, screenModel)
        ButlerDialog(
            modifier = Modifier.zIndex(1f),
            startScreens = listOf(this),
            isDialogOpen = numberOfErrors > 0,
            isDialogFullscreen = false,
            onDismissDialog = screenModel::removeLastError
        )
    }
}

@Composable
private fun ErrorScreen(
    state: ErrorScreenState,
    screenModel: ErrorScreenModel
) {
    val serverErrorContent = @Composable {
        state.serverErrors.maxByOrNull { it.second.timestamp }?.let {
            ButlerErrorDialogContent(
                errorResponse = it.second,
                onClose = { screenModel.clearError(it.first) }
            )
        }
    }
    val appErrorContent = @Composable {
        state.appErrors.maxByOrNull { it.timestamp }?.let {
            ButlerErrorDialogContent(
                errorEvent = it,
                onClose = { screenModel.clearError(it.id) }
            )
        }
    }
    Crossfade(
        modifier = Modifier.animateContentSize(spring()),
        targetState = state.appErrors + state.serverErrors
    ) { _ ->
        val latestAppError = state.appErrors.maxByOrNull { it.timestamp }
        val latestServerError = state.serverErrors.maxByOrNull { it.second.timestamp }
        if (latestAppError != null && latestServerError != null) {
            if (latestAppError.timestamp > latestServerError.second.timestamp) {
                appErrorContent()
            } else {
                serverErrorContent()
            }
        } else if (latestAppError != null) {
            appErrorContent()
        } else if (latestServerError != null) {
            serverErrorContent()
        }
    }
}