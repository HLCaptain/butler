package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

actual fun getPlatformName(): String {
    return "Android"
}

actual fun isDebugBuild() = BuildConfig.DEBUG

@Composable
actual fun getWindowSizeInDp(): Pair<Dp, Dp> {
    val size = LocalConfiguration.current
    return size.screenHeightDp.dp to size.screenWidthDp.dp
}
