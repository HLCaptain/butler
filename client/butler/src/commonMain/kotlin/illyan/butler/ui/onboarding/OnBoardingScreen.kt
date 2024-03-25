package illyan.butler.ui.onboarding

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.ui.login.LoginScreen

class OnBoardingScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<OnBoardingScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
        val navigator = LocalNavigator.currentOrThrow
        Button(
            onClick = { navigator.push(LoginScreen()) }
        ) {
            Text("Click me!")
        }
    }
}