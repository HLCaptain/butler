package illyan.butler.ui.auth_flow

import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import illyan.butler.ui.auth_success.AuthSuccessIcon
import illyan.butler.ui.login.LoginScreen
import illyan.butler.ui.select_host.SelectHost
import illyan.butler.ui.signup.SignUp
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
private data class SignUp(
    val email: String,
    val password: String,
)

@Composable
fun AuthFlow(
    authSuccess: () -> Unit = {},
    authSuccessEnded: () -> Unit
) {
    val authNavController = rememberNavController()
    val animationTime = 200
    NavHost(
        navController = authNavController,
        contentAlignment = Alignment.Center,
        sizeTransform = { SizeTransform() },
        startDestination = "login",
        enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
        popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
        exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
        popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
    ) {
        composable("login") {
            LoginScreen(
                onSignUp = { email, password -> authNavController.navigate(SignUp(email, password)) },
                onSelectHost = { authNavController.navigate("selectHost") },
                onAuthenticated = { authNavController.navigate("authSuccess") { launchSingleTop = true } }
            )
        }
        composable("selectHost") {
            SelectHost {
                authNavController.navigateUp()
            }
        }
        composable<SignUp> {
            val (email, password) = it.toRoute<SignUp>()
            SignUp(
                initialEmail = email,
                initialPassword = password,
                onSignUpSuccessful = {
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
