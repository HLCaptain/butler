package illyan.butler.ui.signup

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.email
import illyan.butler.generated.resources.login
import illyan.butler.generated.resources.password
import illyan.butler.generated.resources.sign_up
import illyan.butler.generated.resources.username
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.LoadingIndicator
import illyan.butler.core.ui.components.smallDialogWidth
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@Composable
fun SignUpScreen(
    initialEmail: String = "",
    initialPassword: String = "",
    signedUp: () -> Unit
) {
    val screenModel = koinViewModel<SignUpViewModel>()
    val state by screenModel.state.collectAsState()
    // Make your Compose Multiplatform UI

    // TODO: implement oath authentication
    // TODO: implement email/password authentication
    // Go back when user is authenticated
    // Login button should navigate to LoginScreen. Go back if it is already on the stack.
    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn == true) signedUp()
    }

    SignUpDialogContent(
        state = state,
        initialEmail = initialEmail,
        initialPassword = initialPassword,
        signUp = screenModel::signUpAndLogin
    )
}

@Composable
fun SignUpDialogContent(
    modifier: Modifier = Modifier,
    state: SignUpScreenState,
    initialEmail: String,
    initialPassword: String,
    signUp: (String, String, String) -> Unit
) {
    Crossfade(
        modifier = modifier,
        targetState = state.isSigningIn,
        label = "Signup Dialog Content"
    ) { userSignedIn ->
        if (userSignedIn) {
            ButlerDialogContent(
                modifier = Modifier.smallDialogWidth(),
                text = { LoadingIndicator() },
                textPaddingValues = PaddingValues()
            )
        } else {
            var username by rememberSaveable { mutableStateOf("") }
            var email by rememberSaveable { mutableStateOf(initialEmail) }
            var password by rememberSaveable { mutableStateOf(initialPassword) }
            ButlerDialogContent(
                modifier = Modifier.smallDialogWidth(),
                title = {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(Res.string.sign_up),
                    )
                },
                text = {
                    SignUpScreen(
                        username = username,
                        email = email,
                        password = password,
                        userNameChanged = { username = it },
                        emailChanged = { email = it },
                        passwordChanged = { password = it }
                    )
                },
                buttons = {
                    SignUpButtons(signUp = { signUp(email, password, username) })
                },
                containerColor = Color.Transparent,
            )
        }
    }
}

@Composable
fun SignUpScreen(
    modifier: Modifier = Modifier,
    username: String,
    email: String,
    password: String,
    userNameChanged: (String) -> Unit = {},
    emailChanged: (String) -> Unit = {},
    passwordChanged: (String) -> Unit = {}
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = username,
            enabled = true,
            onValueChange = userNameChanged,
            label = { Text(text = stringResource(Res.string.username)) }
        )
        OutlinedTextField(
            value = email,
            enabled = true,
            onValueChange = emailChanged,
            label = { Text(text = stringResource(Res.string.email)) }
        )
        OutlinedTextField(
            value = password,
            enabled = true,
            onValueChange = passwordChanged,
            label = { Text(text = stringResource(Res.string.password)) }
        )
    }
}

@Composable
fun SignUpButtons(
    modifier: Modifier = Modifier,
    signUp: (() -> Unit)
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = signUp,
            enabled = true
        ) {
            Text(text = stringResource(Res.string.login))
        }
    }
}
