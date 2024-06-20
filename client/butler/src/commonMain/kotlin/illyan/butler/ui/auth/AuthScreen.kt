package illyan.butler.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import illyan.butler.ui.auth_success.AuthSuccessIcon
import illyan.butler.ui.auth_success.AuthSuccessScreen
import illyan.butler.ui.auth_success.LocalAuthSuccessDone
import illyan.butler.ui.dialog.LocalDialogDismissRequest
import illyan.butler.ui.login.LoginScreen
import illyan.butler.ui.select_host.SelectHostScreen
import illyan.butler.ui.select_host_tutorial.LocalSelectHostCallback
import illyan.butler.ui.signup_tutorial.LocalSignInCallback
import kotlinx.coroutines.delay

@Composable
fun AuthScreen(
    dismiss: () -> Unit
) {
    val viewModel = viewModel<AuthViewModel>()
    val state by viewModel.state.collectAsState()
    // Make your Compose Multiplatform UI

    // Close if user is authenticated!
    // 1. Select host if not set
    // 2. Show login screen
    // 3. Close if user is authenticated

    val close = { dismiss() }

    val loginScreen by lazy { LoginScreen() }

    CompositionLocalProvider(
        LocalAuthSuccessDone provides { close() },
        LocalSignInCallback provides { navigator.replaceAll(authSuccessScreen) },
        LocalSelectHostCallback provides { navigator.push(loginScreen) }
    ) {
        LaunchedEffect(state) {
            if (state.isUserSignedIn == true) close()
            if (state.hostSelected == false) {
                navigator.replace(selecHostScreen)
            } else if (state.hostSelected == true && state.isUserSignedIn == false) {
                navigator.replace(loginScreen)
            }
        }
    }
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "auth",

    ) {
        composable("auth") {
            LoginScreen()
        }
        composable("authSuccess") {
            AuthSuccessIcon()
            LaunchedEffect(Unit) {
                delay(1000L)
                close()
            }
        }
        composable("selectHost") {
            SelectHostScreen {
                navController.navigate("auth")
            }
        }
    }
}
