package illyan.butler.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class OnBoardingScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<OnBoardingScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
        val navigator = LocalNavigator.currentOrThrow

        // (language selection may be in a corner?)
        // Show onboarding screen flow with small tutorials:
        //  - Welcome to Butler and other basic onboarding screens
        //  - Select host to connect
        //  - Sign In flow with oauth option
        //  - Return to Home screen (done automatically after sign in)
    }
}