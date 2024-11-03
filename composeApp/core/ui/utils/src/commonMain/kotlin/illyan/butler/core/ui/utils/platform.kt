package illyan.butler.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

@Composable
expect fun getWindowSizeInDp(): Pair<Dp, Dp>
