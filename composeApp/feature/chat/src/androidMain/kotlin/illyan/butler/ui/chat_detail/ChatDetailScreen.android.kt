package illyan.butler.ui.chat_detail

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.eygraber.compose.permissionx.rememberPermissionState
import illyan.butler.core.ui.components.ButlerDialog
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.open_app_settings
import illyan.butler.generated.resources.permission_request_denied_description
import illyan.butler.generated.resources.permission_request_denied_title
import illyan.butler.ui.permission.PermissionRequestScreen
import org.jetbrains.compose.resources.stringResource

@Composable
actual fun ChatDetailBottomBar(
    modifier: Modifier,
    sendMessage: (String) -> Unit,
    sendImage: (String) -> Unit,
    isRecording: Boolean,
    toggleRecord: () -> Unit
) {
    var showAppRationaleWithPermission by rememberSaveable { mutableStateOf<String?>(null) }

    val galleryPermissionState = rememberPermissionState(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    })
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO)
    MessageField(
        modifier = modifier,
        sendMessage = sendMessage,
        isRecording = isRecording,
        toggleRecord = toggleRecord,
        sendImage = sendImage,
        galleryAccessGranted = galleryPermissionState.status.isGranted,
        galleryEnabled = true,
        recordAudioAccessGranted = recordAudioPermissionState.status.isGranted,
        recordAudioEnabled = true,
        requestGalleryAccess = { showAppRationaleWithPermission = galleryPermissionState.permission },
        requestRecordAudioAccess = { showAppRationaleWithPermission = recordAudioPermissionState.permission }
    )
    ButlerDialog(
        isDialogOpen = showAppRationaleWithPermission != null,
        onDismissDialog = { showAppRationaleWithPermission = null },
    ) {
        val permissionState = remember(showAppRationaleWithPermission, galleryPermissionState, recordAudioPermissionState) {
            when (showAppRationaleWithPermission) {
                galleryPermissionState.permission -> galleryPermissionState
                recordAudioPermissionState.permission -> recordAudioPermissionState
                else -> null
            }
        }
        if (permissionState?.status?.isPermanentlyDenied == true) {
            PermissionRequestScreen(
                title = stringResource(Res.string.permission_request_denied_title),
                description = stringResource(Res.string.permission_request_denied_description),
                requestPermissionText = stringResource(Res.string.open_app_settings),
                onDismiss = { showAppRationaleWithPermission = null },
                onRequestPermission = { permissionState.openAppSettings() }
            )
        } else {
            permissionState?.let {
                PermissionRequestScreen(
                    permission = it.permission,
                    onDismiss = { showAppRationaleWithPermission = null },
                    onRequestPermission = { it.launchPermissionRequest() }
                )
            }
        }
    }
}
