package illyan.butler.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.ui.auth_success.AuthSuccessScreen
import illyan.butler.ui.auth_success.LocalAuthSuccessDone
import illyan.butler.ui.dialog.LocalDialogDismissRequest
import illyan.butler.ui.login.LoginScreen
import illyan.butler.ui.select_host.SelectHostScreen
import illyan.butler.ui.select_host_tutorial.LocalSelectHostCallback
import illyan.butler.ui.signup_tutorial.LocalSignInCallback

class AuthScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<AuthScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        // Make your Compose Multiplatform UI

        // Close if user is authenticated!
        // 1. Select host if not set
        // 2. Show login screen
        // 3. Close if user is authenticated

        val dismissDialog = LocalDialogDismissRequest.current
        val close = { dismissDialog() }

        val authSuccessScreen by lazy { AuthSuccessScreen(1000) }
        val selecHostScreen by lazy { SelectHostScreen() }
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
    }
}
