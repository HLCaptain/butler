package illyan.butler.ui.permission

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerMediumTextButton
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.permission_request_generic_description
import illyan.butler.generated.resources.permission_request_generic_title
import illyan.butler.generated.resources.request_permission
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

expect val platformSpecificPermissions: Map<String, Pair<StringResource?, StringResource?>>

@Composable
fun PermissionRequestScreen(
    modifier: Modifier = Modifier,
    permission: String,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    // Butler dialog screen content, requesting permission
    val (title, description) = platformSpecificPermissions[permission] ?: Pair(null, null)
    PermissionRequestScreen(
        modifier = modifier,
        title = stringResource(title ?: Res.string.permission_request_generic_title),
        description = stringResource(description ?: Res.string.permission_request_generic_description),
        requestPermissionText = stringResource(Res.string.request_permission),
        onDismiss = onDismiss,
        onRequestPermission = onRequestPermission
    )
}

@Composable
fun PermissionRequestScreen(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    requestPermissionText: String,
    onDismiss: () -> Unit,
    onRequestPermission: () -> Unit
) {
    ButlerDialogContent(
        modifier = modifier,
        title = { Text(title) },
        text = { Text(description) },
        buttons = {
            Row {
                ButlerMediumTextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.close))
                }
                ButlerMediumSolidButton(onClick = onRequestPermission) {
                    Text(requestPermissionText)
                }
            }
        }
    )
}