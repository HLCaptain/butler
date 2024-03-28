package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import illyan.butler.config.BuildConfig

actual fun getPlatformName(): String {
    return "JVM"
}

actual fun isDebugBuild() = BuildConfig.DEBUG

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getWindowSizeInDp(): Pair<Dp, Dp> {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current.density
    return size.height.dp / density to size.width.dp / density
}
