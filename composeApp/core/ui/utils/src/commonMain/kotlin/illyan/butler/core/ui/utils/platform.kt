package illyan.butler.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp

/**
 * Returns the size of the window in dp as (height, width).
 */
@Composable
fun getWindowSizeInDp(): Pair<Dp, Dp> {
    val size = LocalWindowInfo.current.containerSize
    return with(LocalDensity.current) { size.height.toDp() to size.width.toDp() }
}

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
