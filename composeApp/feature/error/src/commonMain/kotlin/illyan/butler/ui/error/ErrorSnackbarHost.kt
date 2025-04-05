package illyan.butler.ui.error

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import illyan.butler.domain.model.DomainError
import illyan.butler.domain.model.ErrorCode
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.error_message_response_long
import org.jetbrains.compose.resources.stringResource

@Composable
fun ErrorSnackbarHost(
    modifier: Modifier = Modifier,
    errors: List<DomainError.Event.Simple>,
    cleanError: (String) -> Unit,
) {
    val hostState = remember { SnackbarHostState() }
    val errorCodeMessages = mapOf(
        ErrorCode.MessageResponseError to stringResource(Res.string.error_message_response_long)
    )
    LaunchedEffect(errors) {
        errors.lastOrNull()?.let {
            hostState.showSnackbar(
                message = errorCodeMessages[it.code]!!,
                duration = SnackbarDuration.Short,
                withDismissAction = true
            )
            cleanError(it.id)
        }
    }
    SnackbarHost(
        modifier = modifier,
        hostState = hostState,
    )
}