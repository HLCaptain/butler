package illyan.butler

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import illyan.butler.ui.home.HomeScreen
import illyan.butler.ui.theme.ButlerTheme
import org.koin.compose.KoinContext

@Composable
fun App() {
    KoinContext {
        ButlerTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Navigator(HomeScreen()) { navigator ->
                    SlideTransition(navigator) {
                        it.Content()
                    }
                }
            }
        }
    }
}
