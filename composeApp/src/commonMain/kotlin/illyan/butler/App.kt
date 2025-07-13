package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.ui.home.Home
import illyan.butler.ui.theme.ThemeViewModel
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    val themeViewModel = koinViewModel<ThemeViewModel>()
    LaunchedEffect(Unit) {
        while (true) {
            themeViewModel.calculateIsNight()
            delay(1000)
        }
    }
    val state by themeViewModel.state.collectAsState()
    ButlerTheme(
        theme = state.theme,
        paletteStyle = state.paletteStyle,
        contrast = state.contrast,

        dynamicColorEnabled = state.dynamicColorEnabled,
        isNight = state.isNight,
    ) {
        val libraries = rememberLibraries {
            illyan.composeapp.generated.resources.Res.readBytes("files/aboutlibraries.json").decodeToString()
        }
        Home(
            libraries = libraries
        )
    }
}
