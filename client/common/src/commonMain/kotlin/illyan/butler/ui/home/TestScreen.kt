package illyan.butler.ui.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

class TestScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<TestScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
    }
}