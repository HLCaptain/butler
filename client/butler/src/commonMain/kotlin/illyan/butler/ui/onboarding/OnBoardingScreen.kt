package illyan.butler.ui.onboarding

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.Navigator
import illyan.butler.ui.auth_success.AuthSuccessScreen
import illyan.butler.ui.auth_success.LocalAuthSuccessDone
import illyan.butler.ui.select_host_tutorial.LocalSelectHostCallback
import illyan.butler.ui.select_host_tutorial.SelectHostTutorialScreen
import illyan.butler.ui.signup_tutorial.LocalSignInCallback
import illyan.butler.ui.signup_tutorial.SignUpTutorialScreen
import illyan.butler.ui.usage_tutorial.LocalUsageTutorialDone
import illyan.butler.ui.usage_tutorial.UsageTutorialScreen
import illyan.butler.ui.welcome.LocalWelcomeScreenDone
import illyan.butler.ui.welcome.WelcomeScreen

class OnBoardingScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<OnBoardingScreenModel>()
        var navigator by remember { mutableStateOf<Navigator?>(null) }

        // (language selection may be in a corner?)
        // 1. Show welcome screen
        // 2. Show host selection tutorial
        // 3. Show sign up tutorial
        // 4. Show usage tutorial
        // 5. If done, set tutorial done.

        val usageTutorialScreen by remember { lazy { UsageTutorialScreen() } }
        val authSuccessScreen by remember { lazy { AuthSuccessScreen(1000) } }
        val signUpTutorialScreen by remember { lazy { SignUpTutorialScreen() } }
        val selectHostTutorialScreen by remember { lazy { SelectHostTutorialScreen() } }
        val welcomeScreen by remember { lazy { WelcomeScreen() } }

        CompositionLocalProvider(
            LocalWelcomeScreenDone provides { navigator?.push(selectHostTutorialScreen) },
            LocalSelectHostCallback provides { navigator?.push(signUpTutorialScreen) },
            LocalSignInCallback provides { navigator?.replaceAll(authSuccessScreen) },
            LocalAuthSuccessDone provides { navigator?.replaceAll(usageTutorialScreen) },
            LocalUsageTutorialDone provides { screenModel.setTutorialDone() },
        ) {
            Navigator(screen = welcomeScreen) {
                LaunchedEffect(Unit) {
                    navigator = it
                }
            }
        }
    }
}
