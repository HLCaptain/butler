package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import illyan.butler.repository.permission.PermissionRepository
import illyan.butler.utils.audio.AudioRecorder

expect fun getPlatformName(): String

expect fun isDebugBuild(): Boolean

@Composable
expect fun getWindowSizeInDp(): Pair<Dp, Dp>

expect fun getOsName(): String

expect fun getSystemMetadata(): Map<String, String>

fun canRecordAudio() = getAudioRecorder() != null

expect fun getAudioRecorder(): AudioRecorder?

expect fun getPlatformPermissionRepository(): PermissionRepository
