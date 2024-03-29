package illyan.butler.ui.arbitrary

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen

class ArbitraryScreen(private val content: @Composable () -> Unit) : Screen {
    @Composable
    override fun Content() {
        content()
    }
}