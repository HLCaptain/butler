package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

expect fun getPlatformName(): String

expect fun isDebugBuild(): Boolean

@Composable
expect fun getWindowSizeInDp(): Pair<Dp, Dp>
