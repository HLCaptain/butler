package illyan.butler

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import illyan.butler.ui.home.HomeScreen
import illyan.butler.ui.theme.ThemeScreen
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        ThemeScreen {
            Navigator(HomeScreen()) { navigator ->
                SlideTransition(navigator)
            }
        }.Content()
    }
}
