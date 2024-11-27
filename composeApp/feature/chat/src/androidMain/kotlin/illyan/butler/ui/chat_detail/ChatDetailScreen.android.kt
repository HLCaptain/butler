package illyan.butler.ui.chat_detail

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import illyan.butler.core.ui.components.ButlerDialog
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.open_app_settings
import illyan.butler.generated.resources.permission_request_denied_description
import illyan.butler.generated.resources.permission_request_denied_title
import illyan.butler.ui.permission.PermissionRequestScreen
import io.github.aakira.napier.Napier
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
    if (LocalView.current.isInEditMode) {
        MessageField(
            modifier = modifier,
            sendMessage = sendMessage,
            isRecording = isRecording,
            toggleRecord = toggleRecord,
            sendImage = sendImage,
            galleryAccessGranted = true,
            galleryEnabled = true,
            recordAudioAccessGranted = true,
            recordAudioEnabled = true,
            requestGalleryAccess = {},
            requestRecordAudioAccess = {}
        )
    } else {
        var showAppRationaleWithPermission by rememberSaveable { mutableStateOf<String?>(null) }
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
            requestGalleryAccess = { showAppRationaleWithPermission = galleryPermissionState.permission },
            requestRecordAudioAccess = { showAppRationaleWithPermission = recordAudioPermissionState.permission }
        )
        ButlerDialog(
            isDialogOpen = showAppRationaleWithPermission != null,
            onDismissDialog = { showAppRationaleWithPermission = null },
        ) {
            val permissionState = remember(showAppRationaleWithPermission) {
                when (showAppRationaleWithPermission) {
                    galleryPermissionState.permission -> galleryPermissionState
                    recordAudioPermissionState.permission -> recordAudioPermissionState
                    else -> null
                }
            }
            if (permissionDeniedOnLaunch && permissionState?.status?.shouldShowRationale == false) {
                val context = LocalContext.current
                PermissionRequestScreen(
                    title = stringResource(Res.string.permission_request_denied_title),
                    description = stringResource(Res.string.permission_request_denied_description),
                    requestPermissionText = stringResource(Res.string.open_app_settings),
                    onDismiss = { showAppRationaleWithPermission = null },
                    onRequestPermission = {
                        try {
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                                data = Uri.parse("package:${context.packageName}")
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(this)
                            }
                        } catch (e: ActivityNotFoundException) {
                            Napier.e(e) { "Failed to open app settings" }
                            // Fallback to general settings
                            Intent(Settings.ACTION_APPLICATION_SETTINGS).apply {
                                context.startActivity(this)
                            }
                        }
                    }
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
}
