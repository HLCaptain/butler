package illyan.butler

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import illyan.butler.ui.theme.ThemeScreen
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        Navigator(ThemeScreen())
    }
}
