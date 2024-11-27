package illyan.butler.ui.chat_detail

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import illyan.butler.core.ui.components.ButlerDialog
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.open_app_settings
import illyan.butler.generated.resources.permission_request_denied_description
import illyan.butler.generated.resources.permission_request_denied_title
import illyan.butler.ui.permission.PermissionRequestScreen
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun ChatDetailBottomBar(
    modifier: Modifier,
    sendMessage: (String) -> Unit,
    sendImage: (String) -> Unit,
    isRecording: Boolean,
    toggleRecord: () -> Unit
) {
    var showAppRationaleWithPermission by rememberSaveable { mutableStateOf<PermissionState?>(null) }
    var permissionDeniedOnLaunch by rememberSaveable { mutableStateOf(false) }

    val galleryPermissionState = rememberPermissionState(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }) {
        permissionDeniedOnLaunch = !it
        if (it) showAppRationaleWithPermission = null
    }
    val recordAudioPermissionState = rememberPermissionState(Manifest.permission.RECORD_AUDIO) {
        permissionDeniedOnLaunch = !it
        if (it) showAppRationaleWithPermission = null
    }
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
        requestGalleryAccess = { showAppRationaleWithPermission = galleryPermissionState },
        requestRecordAudioAccess = { showAppRationaleWithPermission = recordAudioPermissionState }
    )
    ButlerDialog(
        isDialogOpen = showAppRationaleWithPermission != null,
        onDismissDialog = { showAppRationaleWithPermission = null },
    ) {
        if (permissionDeniedOnLaunch && showAppRationaleWithPermission?.status?.shouldShowRationale == false) {
            val context = LocalContext.current
            PermissionRequestScreen(
                title = stringResource(Res.string.permission_request_denied_title),
                description = stringResource(Res.string.permission_request_denied_description),
                requestPermissionText = stringResource(Res.string.open_app_settings),
                onDismiss = { showAppRationaleWithPermission = null },
                onRequestPermission = {
                    context.startActivity(Intent(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        Settings.ACTION_APPLICATION_SETTINGS
                    } else {
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    }))
                }
            )
        } else {
            showAppRationaleWithPermission?.let {
                PermissionRequestScreen(
                    permission = it.permission,
                    onDismiss = { showAppRationaleWithPermission = null },
                    onRequestPermission = { it.launchPermissionRequest() }
                )
            }
        }
    }
}
