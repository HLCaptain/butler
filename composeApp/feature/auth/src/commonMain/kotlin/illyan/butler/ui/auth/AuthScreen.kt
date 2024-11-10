package illyan.butler.ui.auth

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import illyan.butler.ui.auth_success.AuthSuccessIcon
import illyan.butler.ui.login.LoginScreen
import illyan.butler.ui.select_host.SelectHostScreen
import illyan.butler.ui.signup.SignUpScreen
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    authSuccess: () -> Unit = {},
    authSuccessEnded: () -> Unit
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val state by viewModel.state.collectAsState()
    // Make your Compose Multiplatform UI

    // Close if user is authenticated!
    // 1. Select host if not set
    // 2. Show login screen
    // 3. Close if user is authenticated

    val authNavController = rememberNavController()
    val animationTime = 200
    NavHost(
        navController = authNavController,
        startDestination = "login",
        enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
        popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
        exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
        popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
    ) {
        composable("login") {
            LoginScreen(
                onSignUp = { email, password -> authNavController.navigate("signUp") },
                onSelectHost = { authNavController.navigate("selectHost") },
                onAuthenticated = { authNavController.navigate("authSuccess") { launchSingleTop = true } }
            )
        }
        composable("selectHost") {
            SelectHostScreen {
                authNavController.navigateUp()
            }
        }
        composable("signUp") {
            SignUpScreen(
                signedUp = {
                    authNavController.navigate("authSuccess") { launchSingleTop = true }
                }
            )
        }
        composable("authSuccess") {
            AuthSuccessIcon()
            LaunchedEffect(Unit) {
                authSuccess()
                delay(1000L)
                authSuccessEnded()
            }
        }
    }
}
