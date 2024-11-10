package illyan.butler.ui.permission

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.smallDialogWidth
import illyan.butler.domain.model.Permission
import illyan.butler.domain.model.PermissionStatus
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.all_permissions_granted_description
import illyan.butler.generated.resources.all_permissions_granted_title
import illyan.butler.generated.resources.permission_request_gallery_description
import illyan.butler.generated.resources.permission_request_gallery_title
import illyan.butler.generated.resources.permission_request_record_audio_description
import illyan.butler.generated.resources.permission_request_record_audio_title
import illyan.butler.generated.resources.request_permission
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@Composable
fun PermissionRequestScreen() {
    // Butler dialog screen content, requesting permission
    val screenModel = koinViewModel<PermissionRequestViewModel>()
    val permissions by screenModel.state.collectAsState()
    var permission by rememberSaveable { mutableStateOf<Permission?>(null) }
    LaunchedEffect(permissions) {
        permission = permissions.filter { it.value is PermissionStatus.Denied && (it.value as PermissionStatus.Denied).shouldShowRationale }.keys.firstOrNull()
    }
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
        AnimatedVisibility(visible = permission != null) {
            Button(
                onClick = {
                    permission?.let { screenModel.launchPermissionRequest(it) }
                }
            ) {
                Text(stringResource(Res.string.request_permission))
            }
        }
    }
    )
}