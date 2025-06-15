package illyan.butler.ui.server.auth_flow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import illyan.butler.core.ui.utils.BackHandler
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.login
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.sign_up
import illyan.butler.generated.resources.unknown
import illyan.butler.ui.auth_success.AuthSuccessIcon
import illyan.butler.ui.server.login.Login
import illyan.butler.ui.server.select_host.SelectHost
import illyan.butler.ui.server.signup.SignUp
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
sealed class AuthFlowDestination {
    @Serializable
    data class Login(
        val currentHost: String,
    ) : AuthFlowDestination()

    @Serializable
    data class SignUp(
        val email: String,
        val password: String,
        val currentHost: String,
    ) : AuthFlowDestination()

    @Serializable
    data object AuthSuccess : AuthFlowDestination()

    @Serializable
    data object SelectHost : AuthFlowDestination()
}

private val NavBackStackEntry.order: Int get() = when {
    destination.hasRoute<AuthFlowDestination.SelectHost>() -> 0
    destination.hasRoute<AuthFlowDestination.Login>() -> 1
    destination.hasRoute<AuthFlowDestination.SignUp>() -> 2
    else -> -1
}

private val NavBackStackEntry?.title: StringResource? get() = this?.let {
    when {
        destination.hasRoute<AuthFlowDestination.SelectHost>() -> Res.string.select_host
        destination.hasRoute<AuthFlowDestination.Login>() -> Res.string.login
        destination.hasRoute<AuthFlowDestination.SignUp>() -> Res.string.sign_up
        else -> null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthFlow(
    authSuccessEnded: () -> Unit,
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<AuthViewModel>()
    val state by viewModel.state.collectAsState()
    val authNavController = rememberNavController()
    val animationTime = 200
    val currentEntry by authNavController.currentBackStackEntryAsState()
    val backStack by authNavController.currentBackStack.collectAsState()
    val isFinalStep = remember(currentEntry) { currentEntry?.destination?.hasRoute<AuthFlowDestination.AuthSuccess>() == true }
    BackHandler(enabled = !isFinalStep && authNavController.previousBackStackEntry != null) {
        authNavController.navigateUp()
    }
    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = currentEntry != null && !isFinalStep,
                enter = slideInVertically(initialOffsetY = { -it }, animationSpec = tween(animationTime)),
                exit = slideOutVertically(targetOffsetY = { -it }, animationSpec = tween(animationTime))
            ) {
                TopAppBar(
                    title = {
                        AnimatedVisibility(visible = currentEntry?.order != -1) {
                            AnimatedContent(
                                targetState = backStack.size to currentEntry,
                                transitionSpec = {
                                    val enterTransition = slideInHorizontally(tween(animationTime * 2)) { it }
                                    val popEnterTransition = slideInHorizontally(tween(animationTime * 2)) { -it }
                                    val exitTransition = slideOutHorizontally(tween(animationTime * 2)) { -it }
                                    val popExitTransition = slideOutHorizontally(tween(animationTime * 2)) { it }
                                    if (initialState.first < targetState.first) {
                                        enterTransition togetherWith exitTransition
                                    } else {
                                        popEnterTransition togetherWith popExitTransition
                                    }
                                }
                            ) {
                                Text(
                                    text = stringResource(it.second.title ?: Res.string.unknown),
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                if (authNavController.previousBackStackEntry == null) {
                                    onBack()
                                } else {
                                    authNavController.navigateUp()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            NavHost(
                modifier = Modifier
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding),
                navController = authNavController,
                contentAlignment = Alignment.Center,
                startDestination = if (state.selectedHost != null) AuthFlowDestination.Login(state.selectedHost!!) else AuthFlowDestination.SelectHost,
                enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
                popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
                exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
                popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
            ) {
                composable<AuthFlowDestination.Login> {
                    Login(
                        currentHost = it.toRoute<AuthFlowDestination.Login>().currentHost,
                        onSignUp = { email, password ->
                            state.selectedHost?.let { selectedHost ->
                                authNavController.navigate(
                                    AuthFlowDestination.SignUp(
                                        email,
                                        password,
                                        selectedHost
                                    )
                                )
                            }
                        },
                        onSelectHost = { authNavController.navigate(AuthFlowDestination.SelectHost) },
                        onAuthenticated = { authNavController.navigate(AuthFlowDestination.AuthSuccess) { launchSingleTop = true } }
                    )
                }
                composable<AuthFlowDestination.SelectHost> {
                    SelectHost {
                        if (authNavController.previousBackStackEntry == null) {
                            // This was the first screen, so we navigate to the login screen
                            authNavController.navigate(AuthFlowDestination.Login) {
                                launchSingleTop = true
                            }
                        } else {
                            // This screen was navigated to from another screen, so we just navigate back
                            authNavController.navigateUp()
                        }
                    }
                }
                composable<AuthFlowDestination.SignUp> {
                    val (email, password, currentHost) = it.toRoute<AuthFlowDestination.SignUp>()
                    SignUp(
                        initialEmail = email,
                        initialPassword = password,
                        currentHost = currentHost,
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
}
