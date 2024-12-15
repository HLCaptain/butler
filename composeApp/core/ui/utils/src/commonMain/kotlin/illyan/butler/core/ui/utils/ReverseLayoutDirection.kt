package illyan.butler.core.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun ReverseLayoutDirection(
    enabled: Boolean = true,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLayoutDirection provides if (enabled) {
            if (LocalLayoutDirection.current == LayoutDirection.Rtl) LayoutDirection.Ltr else LayoutDirection.Rtl
        } else LocalLayoutDirection.current
    ) {
        content()
    }
}
