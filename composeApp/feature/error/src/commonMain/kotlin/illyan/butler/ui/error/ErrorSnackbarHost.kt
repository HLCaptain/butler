@file:OptIn(ExperimentalUuidApi::class)

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
import illyan.butler.generated.resources.error_message_chat_refresh
import illyan.butler.generated.resources.error_message_response_long
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Composable
fun ErrorSnackbarHost(
    modifier: Modifier = Modifier,
    errors: List<DomainError.Event.Simple>,
    cleanError: (Uuid) -> Unit,
) {
    val hostState = remember { SnackbarHostState() }
    val errorCodeMessages = mapOf(
        ErrorCode.MessageResponseError to stringResource(Res.string.error_message_response_long),
        ErrorCode.ChatRefreshError to stringResource(Res.string.error_message_chat_refresh),
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
