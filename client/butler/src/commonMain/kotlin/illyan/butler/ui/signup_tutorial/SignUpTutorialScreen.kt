package illyan.butler.ui.signup_tutorial

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.ui.login.LoginScreen

class SignUpTutorialScreen(private val onSignUp: () -> Unit) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<SignUpTutorialScreenModel>()
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        // Make your Compose Multiplatform UI

        // 1. Show sign up tutorial
        // 2. Show sign up screen
        // 3. Go back on user authentication

        Column {
            Text(text = "Sign up to access your chats everywhere!")
            Button(onClick = {
                navigator.push(LoginScreen(onSignUp))
            }) {
                Text(text = "Go to Sign In screen")
            }
        }
    }
}