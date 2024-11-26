package illyan.butler.ui.permission

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.mohamedrejeb.calf.permissions.Permission
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.smallDialogWidth
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.permission_request_generic_title
import illyan.butler.generated.resources.permission_request_generic_description
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
    onRequestPermission: () -> Unit
) {
    // Butler dialog screen content, requesting permission
    ButlerDialogContent(
        modifier = Modifier.smallDialogWidth(),
        title = {
            Text(
                stringResource(
                    when (permission) {
                        Permission.RecordAudio -> Res.string.permission_request_record_audio_title
                        Permission.ReadImage -> Res.string.permission_request_gallery_title
                        else -> Res.string.permission_request_generic_title
                    }
                )
            )
        },
        text = {
            Text(
                stringResource(
                    when (permission) {
                        Permission.RecordAudio -> Res.string.permission_request_record_audio_description
                        Permission.Gallery -> Res.string.permission_request_gallery_description
                        else -> Res.string.permission_request_generic_description
                    }
                )
            )
        },
        buttons = {
            Row {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(Res.string.close))
                }
                Button(onClick = onRequestPermission) {
                    Text(stringResource(Res.string.request_permission))
                }
            }
        }
    )
}