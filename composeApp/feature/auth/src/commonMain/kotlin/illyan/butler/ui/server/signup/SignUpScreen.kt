package illyan.butler.ui.server.signup

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.imePadding
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
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.components.LoadingIndicator
import illyan.butler.core.ui.components.mediumDialogWidth
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.email
import illyan.butler.generated.resources.login
import illyan.butler.generated.resources.password
import illyan.butler.generated.resources.sign_up
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SignUp(
    initialEmail: String = "",
    initialPassword: String = "",
    onSignUpSuccessful: () -> Unit
) {
    val screenModel = koinViewModel<SignUpViewModel>()
    val state by screenModel.state.collectAsState()
    // Make your Compose Multiplatform UI

    // TODO: implement oath authentication
    // TODO: implement email/password authentication
    // Go back when user is authenticated
    // Login button should navigate to LoginScreen. Go back if it is already on the stack.
    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn == true) onSignUpSuccessful()
    }

    SignUpDialogContent(
        modifier = Modifier.imePadding(),
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
    signUp: (String, String) -> Unit
) {
    Crossfade(
        modifier = modifier,
        targetState = state.isSigningIn,
        label = "Signup Dialog Content"
    ) { userSignedIn ->
        if (userSignedIn) {
            ButlerDialogContent(
                modifier = Modifier.mediumDialogWidth(),
                text = { LoadingIndicator() },
                textPaddingValues = PaddingValues()
            )
        } else {
            var email by rememberSaveable { mutableStateOf(initialEmail) }
            var password by rememberSaveable { mutableStateOf(initialPassword) }
            ButlerDialogContent(
                modifier = Modifier.mediumDialogWidth(),
                title = {
                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = stringResource(Res.string.sign_up),
                    )
                },
                text = {
                    SignUp(
                        email = email,
                        password = password,
                        emailChanged = { email = it },
                        passwordChanged = { password = it }
                    )
                },
                buttons = {
                    SignUpButtons(signUp = { signUp(email, password) })
                },
                containerColor = Color.Transparent,
            )
        }
    }
}

@Composable
fun SignUp(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    emailChanged: (String) -> Unit = {},
    passwordChanged: (String) -> Unit = {}
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ButlerTextField(
            value = email,
            enabled = true,
            isOutlined = false,
            onValueChange = emailChanged,
            label = { Text(text = stringResource(Res.string.email)) }
        )
        ButlerTextField(
            value = password,
            enabled = true,
            isOutlined = false,
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
        ButlerMediumSolidButton(
            onClick = signUp,
            enabled = true
        ) {
            Text(text = stringResource(Res.string.login))
        }
    }
}
