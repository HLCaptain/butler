package illyan.butler.ui.server.auth_flow

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
import illyan.butler.ui.server.login.Login
import illyan.butler.ui.server.select_host.SelectHost
import illyan.butler.ui.server.signup.SignUp
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable

@Serializable
sealed class AuthFlowDestination {
    @Serializable
    data object Login : AuthFlowDestination()

    @Serializable
    data class SignUp(
        val email: String,
        val password: String,
    ) : AuthFlowDestination()

    @Serializable
    data object AuthSuccess : AuthFlowDestination()

    @Serializable
    data object SelectHost : AuthFlowDestination()
}

@Composable
fun AuthFlow(
    authSuccessEnded: () -> Unit
) {
    val authNavController = rememberNavController()
    val animationTime = 200
    NavHost(
        navController = authNavController,
        contentAlignment = Alignment.Center,
        sizeTransform = { SizeTransform() },
        startDestination = AuthFlowDestination.Login,
        enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
        popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
        exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
        popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
    ) {
        composable<AuthFlowDestination.Login> {
            Login(
                onSignUp = { email, password -> authNavController.navigate(
                    AuthFlowDestination.SignUp(
                        email,
                        password
                    )
                ) },
                onSelectHost = { authNavController.navigate(AuthFlowDestination.SelectHost) },
                onAuthenticated = { authNavController.navigate(AuthFlowDestination.AuthSuccess) { launchSingleTop = true } }
            )
        }
        composable<AuthFlowDestination.SelectHost> {
            SelectHost {
                authNavController.navigateUp()
            }
        }
        composable<AuthFlowDestination.SignUp> {
            val (email, password) = it.toRoute<AuthFlowDestination.SignUp>()
            SignUp(
                initialEmail = email,
                initialPassword = password,
                onSignUpSuccessful = {
                    authNavController.navigate(AuthFlowDestination.AuthSuccess) { launchSingleTop = true }
                }
            )
        }
        composable<AuthFlowDestination.AuthSuccess> {
            AuthSuccessIcon()
            LaunchedEffect(Unit) {
                delay(1000L)
                authSuccessEnded()
            }
        }
    }
}
