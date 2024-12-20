package illyan.butler.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.sign_in_anonymously
import illyan.butler.generated.resources.sign_up
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.LoadingIndicator
import illyan.butler.core.ui.components.MenuButton
import illyan.butler.core.ui.components.smallDialogWidth
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun LoginScreen(
    onSignUp: (String, String) -> Unit,
    onSelectHost: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val screenModel = koinViewModel<LoginViewModel>()
    val state by screenModel.state.collectAsState()

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn == true) onAuthenticated()
    }
    // TODO: implement oath authentication
    LoginDialogContent(
        isUserSigningIn = state.isSigningIn,
        signInAnonymously = {}, // TODO: Implement sign in anonymously
        signInWithEmailAndPassword = screenModel::signInWithEmailAndPassword,
        navigateToSignUp = onSignUp,
        selectHost = onSelectHost
    )
}

@Composable
fun LoginDialogContent(
    modifier: Modifier = Modifier,
    isUserSigningIn: Boolean = false,
    signInAnonymously: (() -> Unit)? = null,
    signInWithEmailAndPassword: (email: String, password: String) -> Unit = { _, _ -> },
    navigateToSignUp: (String, String) -> Unit = { _, _ -> },
    selectHost: (() -> Unit)? = null
) {
    Crossfade(
        modifier = modifier,
        targetState = isUserSigningIn,
        label = "Login Dialog Content"
    ) { userSignedIn ->
        if (userSignedIn) {
            ButlerDialogContent(
                modifier = Modifier.smallDialogWidth(),
                text = { LoadingIndicator() },
                textPaddingValues = PaddingValues()
            )
        } else {
            var email by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }
            ButlerDialogContent(
                modifier = Modifier.smallDialogWidth(),
                title = {
                    Text(text = stringResource(Res.string.login))
                },
                text = {
                    LoginScreen(
                        emailChanged = { email = it },
                        passwordChanged = { password = it }
                    )
                },
                buttons = {
                    LoginButtons(
                        signInWithEmailAndPassword = { signInWithEmailAndPassword(email, password) },
                        signInAnonymously = signInAnonymously,
                        navigateToSignUp = { navigateToSignUp(email, password) },
                        selectHost = selectHost
                    )
                },
                containerColor = Color.Transparent,
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun LoginScreen(
    modifier: Modifier = Modifier,
    emailChanged: (String) -> Unit = {},
    passwordChanged: (String) -> Unit = {}
) {
    Column(modifier = modifier) {
        var email by rememberSaveable { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            enabled = true,
            onValueChange = {
                email = it
                emailChanged(it)
            },
            label = { Text(text = stringResource(Res.string.email)) }
        )
        var password by rememberSaveable { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            enabled = true,
            onValueChange = {
                password = it
                passwordChanged(it)
            },
            label = { Text(text = stringResource(Res.string.password)) }
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginButtons(
    modifier: Modifier = Modifier,
    signInAnonymously: (() -> Unit)? = null,
    signInWithEmailAndPassword: (() -> Unit)? = null,
    navigateToSignUp: (() -> Unit),
    selectHost: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = signInWithEmailAndPassword != null
        ) {
            Button(
                onClick = signInWithEmailAndPassword ?: {},
                enabled = true
            ) {
                Text(text = stringResource(Res.string.login))
            }
        }

        HorizontalDivider()

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MenuButton(
                onClick = navigateToSignUp,
                enabled = true,
                text = stringResource(Res.string.sign_up)
            )

            AnimatedVisibility(visible = signInAnonymously != null) {
                MenuButton(
                    onClick = signInAnonymously ?: {},
                    enabled = true,
                    text = stringResource(Res.string.sign_in_anonymously)
                )
            }
        }

        HorizontalDivider()

        AnimatedVisibility(visible = selectHost != null) {
            MenuButton(
                onClick = selectHost ?: {},
                enabled = true,
                text = stringResource(Res.string.select_host)
            )
        }
    }
}

//@Preview
@Composable
fun LoginScreenPreview() {
    LoginDialogContent(
        isUserSigningIn = false,
        signInAnonymously = {},
        signInWithEmailAndPassword = { _, _ -> },
        navigateToSignUp = { _, _ -> },
        selectHost = {}
    )
}
