package illyan.butler.ui.permission

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.smallDialogWidth
import illyan.butler.domain.model.Permission
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.all_permissions_granted_description
import illyan.butler.generated.resources.all_permissions_granted_title
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.permission_request_gallery_description
import illyan.butler.generated.resources.permission_request_gallery_title
import illyan.butler.generated.resources.permission_request_record_audio_description
import illyan.butler.generated.resources.permission_request_record_audio_title
import illyan.butler.generated.resources.request_permission
import org.jetbrains.compose.resources.stringResource

@Composable
fun PermissionRequestScreen(
    permission: Permission,
    onDismiss: () -> Unit,
    requestPermission: () -> Unit
) {
    // Butler dialog screen content, requesting permission
    ButlerDialogContent(
        modifier = Modifier.smallDialogWidth(),
        title = {
            Text(
                stringResource(
                    when (permission) {
                        Permission.RECORD_AUDIO -> Res.string.permission_request_record_audio_title
                        Permission.GALLERY -> Res.string.permission_request_gallery_title
                        else -> Res.string.all_permissions_granted_title
                    }
                )
            )
        },
        text = {
            Text(
                stringResource(
                    when (permission) {
                        Permission.RECORD_AUDIO -> Res.string.permission_request_record_audio_description
                        Permission.GALLERY -> Res.string.permission_request_gallery_description
                        else -> Res.string.all_permissions_granted_description
                    }
                )
            )
        },
        buttons = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.close))
                }
                Button(onClick = requestPermission) {
                    Text(stringResource(Res.string.request_permission))
                }
            }
        }
    )
}