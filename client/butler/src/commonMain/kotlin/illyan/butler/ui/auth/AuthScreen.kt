package illyan.butler.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

class AuthScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<AuthScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI

        // 1. Select host if not set
        // 2. Show login screen
    }
}