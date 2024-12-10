package illyan.butler.ui.signup_tutorial

import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import illyan.butler.core.ui.components.largeDialogWidth
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.butler_logo_alternative
import illyan.butler.generated.resources.sign_up_tutorial
import illyan.butler.ui.auth_flow.AuthFlowDestination
import illyan.butler.ui.auth_success.AuthSuccessIcon
import illyan.butler.ui.login.Login
import illyan.butler.ui.select_host.SelectHost
import illyan.butler.ui.signup.SignUp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SignUpTutorial(
    authSuccessEnded: () -> Unit
) {
    val authNavController = rememberNavController()
    val animationTime = 200
    Column(
        modifier = Modifier.largeDialogWidth().padding(16.dp).safeContentPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Image(
            modifier = Modifier.clip(RoundedCornerShape(16.dp)).weight(1f, fill = false),
            contentScale = ContentScale.Fit,
            painter = painterResource(Res.drawable.butler_logo_alternative),
            contentDescription = "Butler Alternative Logo"
        )
        Column(
            modifier = Modifier.padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(Res.string.sign_up_tutorial),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
        }
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
                    onSignUp = { email, password -> authNavController.navigate(AuthFlowDestination.SignUp(email, password)) },
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
}
