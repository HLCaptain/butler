package illyan.butler.ui.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerMediumTextButton
import illyan.butler.domain.model.DomainErrorEvent
import illyan.butler.domain.model.DomainErrorResponse
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.copy_stacktrace
import illyan.butler.generated.resources.hello_x
import io.ktor.http.HttpStatusCode
import org.jetbrains.compose.resources.stringResource

// Error dialog should contain a button copying the stacktrace to clipboard
// Error dialog should provide info or link to submit a bug report
// Error dialog should provide a brief error message or code if present

@Composable
fun ButlerErrorDialogContent(
    modifier: Modifier = Modifier,
    icon: (@Composable ColumnScope.() -> Unit)? = null,
    title: (@Composable ColumnScope.() -> Unit)? = null,
    text: (@Composable ColumnScope.() -> Unit)? = null,
    buttons: (@Composable ColumnScope.() -> Unit)? = null
) = ButlerDialogContent(
    modifier = modifier,
    icon = icon,
    title = title,
    text = text,
    buttons = buttons
)

@Composable
fun ButlerErrorDialogContent(
    modifier: Modifier = Modifier,
    errorResponse: DomainErrorResponse,
    onClose: () -> Unit = {},
    icon: (@Composable ColumnScope.() -> Unit)? = {
        Icon(
            Icons.Rounded.CloudOff,
            contentDescription = "An error occurred"
        )
    },
    title: (@Composable ColumnScope.() -> Unit)? = {
        Text(stringResource(Res.string.hello_x).format(errorResponse.httpStatusCode))
    },
    text: (@Composable ColumnScope.() -> Unit)? = {
        Text(HttpStatusCode.fromValue(errorResponse.httpStatusCode).description)
    },
    buttons: (@Composable ColumnScope.() -> Unit)? = {
        ButlerMediumSolidButton(
            text = { Text(stringResource(Res.string.close)) },
            onClick = onClose
        )
    }
) = ButlerErrorDialogContent(
    modifier = modifier,
    icon = icon,
    title = title,
    text = text,
    buttons = buttons
)

@Composable
fun ButlerErrorDialogContent(
    modifier: Modifier = Modifier,
    errorEvent: DomainErrorEvent,
    onClose: () -> Unit = {},
    icon: (@Composable ColumnScope.() -> Unit)? = {
        Icon(
            Icons.Rounded.Error,
            contentDescription = "An error occurred"
        )
    },
    title: (@Composable ColumnScope.() -> Unit)? = {
        Text(errorEvent.exception)
    },
    text: (@Composable ColumnScope.() -> Unit)? = {
        Text(errorEvent.message)
    },
    buttons: (@Composable ColumnScope.() -> Unit)? = {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ButlerMediumTextButton(
                text = { Text(stringResource(Res.string.close)) },
                onClick = onClose
            )
            val clipboardManager = LocalClipboardManager.current
            Button(
                content = { Text(stringResource(Res.string.copy_stacktrace)) },
                onClick = {
                    clipboardManager.setText(buildAnnotatedString { append(errorEvent.stackTrace) })
                }
            )
        }
    }
) = ButlerDialogContent(
    modifier = modifier,
    icon = icon,
    title = title,
    text = text,
    buttons = buttons
)