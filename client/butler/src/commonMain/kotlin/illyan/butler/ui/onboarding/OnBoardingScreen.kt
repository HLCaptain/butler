package illyan.butler.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.ui.auth_success.AuthSuccessScreen
import illyan.butler.ui.dialog.LocalDialogDismissRequest
import illyan.butler.ui.select_host_tutorial.SelectHostTutorialScreen
import illyan.butler.ui.signup_tutorial.SignUpTutorialScreen
import illyan.butler.ui.usage_tutorial.UsageTutorialScreen
import illyan.butler.ui.welcome.WelcomeScreen

class OnBoardingScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<OnBoardingScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        // (language selection may be in a corner?)
        // 1. Show welcome screen
        // 2. Show host selection tutorial
        // 3. Show sign up tutorial
        // 4. Show usage tutorial
        // 5. If done, set tutorial done.

        val dismissDialog = LocalDialogDismissRequest.current
        LaunchedEffect(state.isTutorialDone) {
            if (state.isTutorialDone) {
//                navigator.popUntilRoot()
//                dismissDialog()
            }
        }

        LaunchedEffect(Unit) {
            val usageTutorialScreen by lazy { UsageTutorialScreen { screenModel.setTutorialDone() }}
            val authSuccessScreen by lazy { AuthSuccessScreen(1000) { navigator.replaceAll(usageTutorialScreen) }}
            val signUpTutorialScreen by lazy { SignUpTutorialScreen { navigator.replaceAll(authSuccessScreen) }}
            val selectHostTutorialScreen by lazy { SelectHostTutorialScreen { navigator.push(signUpTutorialScreen) }}
            val welcomeScreen by lazy { WelcomeScreen { navigator.push(selectHostTutorialScreen) }}
            navigator.push(welcomeScreen)
        }
    }
}
