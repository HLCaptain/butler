package illyan.butler.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
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
                dismissDialog()
            }
        }

        LaunchedEffect(Unit) {
            if (navigator.lastItem !is WelcomeScreen) {
                navigator.push(WelcomeScreen {
                    navigator.push(SelectHostTutorialScreen {
                        navigator.push(UsageTutorialScreen {
                            screenModel.setTutorialDone()
                        })
                    })
                })
            }
        }

        var welcomeScreenShown by rememberSaveable { mutableStateOf(false) }
        var usageTutorialShown by rememberSaveable { mutableStateOf(false) }
        OnBoardingScreen(
            state = state,
            welcomeScreenShown = welcomeScreenShown,
            usageTutorialShown = usageTutorialShown,
            showWelcomeScreen = {

            },
            showHostSelectionTutorial = {
                if (navigator.lastItem !is SelectHostTutorialScreen) {
                    navigator.push(SelectHostTutorialScreen {  })
                }
            },
            showSignUpTutorial = {
                if (navigator.lastItem !is SignUpTutorialScreen) {
                    navigator.push(SelectHostTutorialScreen {  })
                }
            },
            showUsageTutorial = {
                if (navigator.lastItem !is UsageTutorialScreen) {
                    navigator.push(UsageTutorialScreen { usageTutorialShown = true })
                }
            },
            onboardingDone = {
                screenModel.setTutorialDone()
            }
        )
    }
}

@Composable
fun OnBoardingScreen(
    state: OnBoardingState,
    welcomeScreenShown: Boolean,
    usageTutorialShown: Boolean,
    showWelcomeScreen: () -> Unit,
    showHostSelectionTutorial: () -> Unit,
    showSignUpTutorial: () -> Unit,
    showUsageTutorial: () -> Unit,
    onboardingDone: () -> Unit
) {
    Crossfade(
        targetState = Triple(state, welcomeScreenShown, usageTutorialShown)
    ) { (targetState, welcomeShown, usageShown) ->
        when {
            !welcomeShown -> {
                showWelcomeScreen()
            }
            !targetState.isHostSelected -> {
                showHostSelectionTutorial()
            }
            !targetState.isUserSignedIn -> {
                showSignUpTutorial()
            }
            !usageShown -> {
                showUsageTutorial()
            }
            else -> {
                onboardingDone()
            }
        }
    }
}