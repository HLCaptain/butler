package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

expect fun getPlatformName(): String

expect fun isDebugBuild(): Boolean

@Composable
expect fun getWindowSizeInDp(): Pair<Dp, Dp>

expect fun getOsName(): String

expect fun getSystemMetadata(): Map<String, String>

expect fun canRecordAudio(): Boolean
