package illyan.butler.ui.signup_tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

class SignUpTutorialScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<SignUpTutorialScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI

        // 1. Show sign up tutorial
        // 2. Show sign up screen
        // 3. Go back on user authentication
    }
}