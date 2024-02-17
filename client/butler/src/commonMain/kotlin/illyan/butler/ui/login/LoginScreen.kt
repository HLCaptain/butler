package illyan.butler.ui.login

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.cancel
import illyan.butler.generated.resources.email
import illyan.butler.generated.resources.login
import illyan.butler.generated.resources.password
import illyan.butler.generated.resources.sign_in_anonymously
import illyan.butler.generated.resources.sign_up
import illyan.butler.ui.components.ButlerDialogContent
import illyan.butler.ui.components.LoadingIndicator
import illyan.butler.ui.dialog.LocalDialogDismissRequest
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class LoginScreen : Screen {
    @Composable
    override fun Content() {
        LoginDialogScreen(
            screenModel = getScreenModel<LoginScreenModel>()
        )
    }

    @Composable
    fun LoginDialogScreen(
        screenModel: LoginScreenModel,
    ) {
        val isUserSignedIn by screenModel.isUserSignedIn.collectAsState()
        val isUserSigningIn by screenModel.isSigningIn.collectAsState()
        val dismissDialog = LocalDialogDismissRequest.current
        LaunchedEffect(isUserSignedIn) {
            if (isUserSignedIn) dismissDialog()
        }
        LoginDialogContent(
            modifier = Modifier.fillMaxWidth(),
            isUserSigningIn = isUserSigningIn,
            signInAnonymously = screenModel::signInAnonymously,
            signInWithEmailAndPassword = screenModel::signInWithEmailAndPassword,
            signUpWithEmailAndPassword = screenModel::signUpWithEmailAndPassword,
        )
    }
}

@Composable
fun LoginDialogContent(
    modifier: Modifier = Modifier,
    isUserSigningIn: Boolean = false,
    signInAnonymously: () -> Unit = {},
    signInWithEmailAndPassword: (email: String, password: String) -> Unit = { _, _ -> },
    signUpWithEmailAndPassword: (email: String, password: String) -> Unit = { _, _ -> },
) {
    Crossfade(
        modifier = modifier,
        targetState = isUserSigningIn,
        label = "Login Dialog Content"
    ) { userSignedIn ->
        if (userSignedIn) {
            ButlerDialogContent(
                text = { LoadingIndicator() },
                textPaddingValues = PaddingValues()
            )
        } else {
            var email by rememberSaveable { mutableStateOf("") }
            var password by rememberSaveable { mutableStateOf("") }
            ButlerDialogContent(
                title = { LoginTitle() },
                text = {
                    LoginScreen(
                        modifier = Modifier.fillMaxWidth(),
                        signInAnonymously = signInAnonymously,
                        emailChanged = { email = it },
                        passwordChanged = { password = it },
                    )
                },
                buttons = {
                    LoginButtons(
                        modifier = Modifier.fillMaxWidth(),
                        signInWithEmailAndPassword = { signInWithEmailAndPassword(email, password) },
                        signUpWithEmailAndPassword = { signUpWithEmailAndPassword(email, password) },
                    )
                },
                containerColor = Color.Transparent,
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginTitle() {
    Text(text = stringResource(Res.string.login))
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    signInAnonymously: () -> Unit = {},
    emailChanged: (String) -> Unit = {},
    passwordChanged: (String) -> Unit = {},
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = signInAnonymously
        ) {
            Text(
                text = stringResource(Res.string.sign_in_anonymously),
                textAlign = TextAlign.Center
            )
        }
        // TODO: make login via email/password combo
        var email by remember { mutableStateOf("") }
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
        var password by remember { mutableStateOf("") }
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
    signInWithEmailAndPassword: () -> Unit = {},
    signUpWithEmailAndPassword: () -> Unit = {},
) {
    val onDialogClosed = LocalDialogDismissRequest.current
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(
            onClick = { onDialogClosed() }
        ) {
            Text(text = stringResource(Res.string.cancel))
        }
        Button(
            onClick = signInWithEmailAndPassword,
            enabled = true,
        ) {
            Text(text = stringResource(Res.string.login))
        }
        Button(
            onClick = signUpWithEmailAndPassword,
            enabled = true,
        ) {
            Text(text = stringResource(Res.string.sign_up))
        }
    }
}
