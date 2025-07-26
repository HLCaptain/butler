package illyan.butler.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import illyan.butler.core.ui.components.ButlerDialogSurface
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.core.utils.getSystemMetadata
import illyan.butler.domain.model.Error
import illyan.butler.domain.model.ErrorState
import illyan.butler.ui.error.ButlerErrorDialogContent
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Preview
@Composable
fun ErrorEventPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            ButlerErrorDialogContent(
                errorEvent = Error.Event.Rich(
                    id = remember { Uuid.random() },
                    platform = "Android",
                    exception = "NetworkErrorException",
                    message = "Network error occurred",
                    stackTrace = "java.net.NetworkErrorException: Network error occurred",
                    metadata = getSystemMetadata(),
                    os = "Android 11",
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    state = ErrorState.NEW
                )
            )
        }
    }
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Preview
@Composable
fun ErrorResponsePreview() {
    ButlerTheme {
        ButlerDialogSurface {
            ButlerErrorDialogContent(
                errorResponse = Error.Response(
                    id = remember { Uuid.random() },
                    customErrorCode = 1234,
                    httpStatusCode = 404,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    message = "Resource not found",
                )
            )
        }
    }
}