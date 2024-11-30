package illyan.butler.ui.onboard_flow

import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import illyan.butler.ui.auth_flow.AuthFlow
import illyan.butler.ui.select_host.SelectHost
import illyan.butler.ui.select_host_tutorial.SelectHostTutorial
import illyan.butler.ui.signup_tutorial.SignUpTutorial
import illyan.butler.ui.usage_tutorial.UsageTutorial
import illyan.butler.ui.welcome.Welcome

@Composable
fun OnboardFlow(
    onTutorialDone: () -> Unit,
) {
    val navController = rememberNavController()
    val animationTime = 200
    val navigationOrder = listOf(
        "welcome",
        "selectHostTutorial",
        "selectHost",
        "signUpTutorial",
        "auth",
        "usageTutorial"
    )
    Column {
        NavHost(
            navController = navController,
            contentAlignment = Alignment.Center,
            sizeTransform = { SizeTransform(clip = false) },
            startDestination = "welcome",
            enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
            popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
            exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
            popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
        ) {
            composable("welcome") {
                Welcome {
                    navController.navigate("selectHostTutorial")
                }
            }
            composable("selectHostTutorial") {
                SelectHostTutorial {
                    navController.navigate("selectHost")
                }
            }
            composable("selectHost") {
                SelectHost {
                    navController.navigate("signUpTutorial")
                }
            }
            composable("signUpTutorial") {
                SignUpTutorial {
                    navController.navigate("auth")
                }
            }
            composable("auth") {
                AuthFlow(
                    authSuccessEnded = {
                        navController.navigate("usageTutorial") {
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable("usageTutorial") {
                UsageTutorial(onTutorialDone = onTutorialDone)
            }
        }
        val nextDestination = remember(navController.currentDestination) {
            try {
                navigationOrder[navigationOrder.indexOf(navController.currentDestination?.route) + 1]
            } catch (e: IndexOutOfBoundsException) {
                null
            }
        }
        val previousDestination = remember(navController.currentDestination) {
            try {
                navigationOrder[navigationOrder.indexOf(navController.currentDestination?.route) - 1]
            } catch (e: IndexOutOfBoundsException) {
                null
            }
        }
        OnboardingProgressBar(
            modifier = Modifier.systemBarsPadding().imePadding(),
            onNext = { nextDestination?.let { navController.navigate(it) } },
            onBack = { previousDestination?.let { navController.navigate(it) } },
            canGoForward = nextDestination != null,
            canGoBack = previousDestination != null
        )
    }
}

@Composable
fun OnboardingProgressBar(
    modifier: Modifier = Modifier,
    onNext: () -> Unit,
    onBack: () -> Unit,
    canGoBack: Boolean = true,
    canGoForward: Boolean = true
) {

}