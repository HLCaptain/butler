package illyan.butler.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

@Composable
expect fun getWindowSizeInDp(): Pair<Dp, Dp>

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)
