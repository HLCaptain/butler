package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import illyan.butler.ui.home.Home
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.ui.theme.ThemeViewModel
import org.koin.compose.KoinContext
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    KoinContext {
        val themeViewModel = koinViewModel<ThemeViewModel>()
        val state by themeViewModel.state.collectAsState()
        ButlerTheme(
            theme = state.theme,
            dynamicColorEnabled = state.dynamicColorEnabled,
            isNight = state.isNight,
        ) {
            Home()
        }
    }
}
