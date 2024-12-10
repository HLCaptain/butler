package illyan.butler.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import illyan.butler.core.ui.components.ButlerDialogSurface
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.core.utils.getSystemMetadata
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.domain.model.ErrorState
import illyan.butler.ui.error.ButlerErrorDialogContent
import kotlinx.datetime.Clock

@Preview
@Composable
fun ErrorEventPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            ButlerErrorDialogContent(
                errorEvent = DomainErrorEvent(
                    id = "1",
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

@Preview
@Composable
fun ErrorResponsePreview() {
    ButlerTheme {
        ButlerDialogSurface {
            ButlerErrorDialogContent(
                errorResponse = DomainErrorResponse(
                    customErrorCode = 1234,
                    httpStatusCode = 404,
                    timestamp = Clock.System.now().toEpochMilliseconds(),
                    message = "Resource not found",
                )
            )
        }
    }
}