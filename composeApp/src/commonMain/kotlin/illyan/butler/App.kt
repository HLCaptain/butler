package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.ui.home.Home
import illyan.butler.ui.theme.ThemeViewModel
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
    val themeViewModel = koinViewModel<ThemeViewModel>()
    val state by themeViewModel.state.collectAsState()
    ButlerTheme(
        theme = state.theme,
        dynamicColorEnabled = state.dynamicColorEnabled,
        isNight = state.isNight,
    ) {
        val libraries by rememberLibraries {
            illyan.composeapp.generated.resources.Res.readBytes("files/aboutlibraries.json").decodeToString()
        }
        Home(
            libraries = libraries
        )
    }
}
