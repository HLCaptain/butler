package illyan.butler.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.ui.dialog.LocalDialogDismissRequest
import illyan.butler.ui.login.LoginScreen
import illyan.butler.ui.select_host.SelectHostScreen

class AuthScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<AuthScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        // Make your Compose Multiplatform UI

        // Close if user is authenticated!
        // 1. Select host if not set
        // 2. Show login screen
        // 3. Close if user is authenticated

        AuthDialogContent(
            state = state,
            navigator = navigator
        )
    }
}

@Composable
fun AuthDialogContent(
    state: AuthState,
    navigator: Navigator
) {
    val dismissDialog = LocalDialogDismissRequest.current
    LaunchedEffect(state) {
        if (state.isUserSignedIn) {
            navigator.popUntilRoot()
            dismissDialog()
        } else {
            if (state.hostSelected == false) {
                navigator.replace(SelectHostScreen {
                    navigator.push(LoginScreen {
                        navigator.popUntilRoot()
                        dismissDialog()
                    })
                })
            } else if (state.hostSelected == true) {
                navigator.replace(LoginScreen {
                    navigator.popUntilRoot()
                    dismissDialog()
                })
            }
        }
    }
}
