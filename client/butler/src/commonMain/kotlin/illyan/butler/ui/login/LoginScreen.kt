package illyan.butler.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.email
import illyan.butler.generated.resources.login
import illyan.butler.generated.resources.password
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.sign_in_anonymously
import illyan.butler.generated.resources.sign_up
import illyan.butler.ui.components.ButlerDialogContent
import illyan.butler.ui.components.LoadingIndicator
import illyan.butler.ui.components.MenuButton
import illyan.butler.ui.dialog.LocalDialogDismissRequest
import illyan.butler.ui.select_host.SelectHostScreen
import illyan.butler.ui.signup.SignUpScreen
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<LoginScreenModel>()
        val state by screenModel.state.collectAsState()

        val dismissDialog = LocalDialogDismissRequest.current
        LaunchedEffect(state.isSignedIn) {
            if (state.isSignedIn) dismissDialog()
        }
        val navigator = LocalNavigator.currentOrThrow
        LoginDialogContent(
            modifier = Modifier.fillMaxWidth(),
            isUserSigningIn = state.isSigningIn,
            signInWithEmailAndPassword = screenModel::signInWithEmailAndPassword,
            navigateToSignUp = { navigator.push(SignUpScreen()) },
            selectHost = { navigator.push(SelectHostScreen()) }
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginDialogContent(
    modifier: Modifier = Modifier,
    isUserSigningIn: Boolean = false,
    signInAnonymously: (() -> Unit)? = null,
    signInWithEmailAndPassword: (email: String, password: String) -> Unit = { _, _ -> },
    navigateToSignUp: (() -> Unit)? = null,
    selectHost: (() -> Unit)? = null
) {
    Crossfade(
        modifier = modifier,
        targetState = isUserSigningIn,
        label = "Login Dialog Content"
    ) { userSignedIn ->
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (userSignedIn) {
                ButlerDialogContent(
                    text = { LoadingIndicator() },
                    textPaddingValues = PaddingValues()
                )
            } else {
                var email by rememberSaveable { mutableStateOf("") }
                var password by rememberSaveable { mutableStateOf("") }
                ButlerDialogContent(
                    title = {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = stringResource(Res.string.login),
                        )
                    },
                    text = {
                        LoginScreen(
                            modifier = Modifier.fillMaxWidth(),
                            emailChanged = { email = it },
                            passwordChanged = { password = it }
                        )
                    },
                    buttons = {
                        LoginButtons(
                            modifier = Modifier.fillMaxWidth(),
                            signInWithEmailAndPassword = { signInWithEmailAndPassword(email, password) },
                            signInAnonymously = signInAnonymously,
                            navigateToSignUp = navigateToSignUp,
                            selectHost = selectHost
                        )
                    },
                    containerColor = Color.Transparent,
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginScreen(
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
            label = {
                Text(text = stringResource(Res.string.email))
            }
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
            label = {
                Text(text = stringResource(Res.string.password))
            }
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginButtons(
    modifier: Modifier = Modifier,
    signInAnonymously: (() -> Unit)? = null,
    signInWithEmailAndPassword: (() -> Unit)? = null,
    navigateToSignUp: (() -> Unit)? = null,
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
            AnimatedVisibility(
                visible = navigateToSignUp != null
            ) {
                MenuButton(
                    onClick = navigateToSignUp ?: {},
                    enabled = true,
                    text = stringResource(Res.string.sign_up)
                )
            }

            AnimatedVisibility(visible = signInAnonymously != null) {
                MenuButton(
                    onClick = signInAnonymously ?: {},
                    enabled = true,
                    text = stringResource(Res.string.sign_in_anonymously)
                )
            }
        }

        HorizontalDivider()

        AnimatedVisibility(
            visible = selectHost != null
        ) {
            MenuButton(
                onClick = selectHost ?: {},
                enabled = true,
                text = stringResource(Res.string.select_host)
            )
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginDialogContent(
        isUserSigningIn = false,
        signInAnonymously = {},
        signInWithEmailAndPassword = { _, _ -> },
        navigateToSignUp = {},
        selectHost = {}
    )
}
