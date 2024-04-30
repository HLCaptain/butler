package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import illyan.butler.utils.AudioRecorder

expect fun getPlatformName(): String

expect fun isDebugBuild(): Boolean

@Composable
expect fun getWindowSizeInDp(): Pair<Dp, Dp>

expect fun getOsName(): String

expect fun getSystemMetadata(): Map<String, String>

fun canRecordAudio() = getAudioRecorder() != null

expect fun getAudioRecorder(): AudioRecorder?
