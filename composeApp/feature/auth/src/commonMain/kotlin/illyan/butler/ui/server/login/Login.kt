package illyan.butler.ui.server.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.ButlerLargeSolidButton
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.components.MenuButton
import illyan.butler.core.ui.components.SmallCircularProgressIndicator
import illyan.butler.core.ui.components.mediumDialogWidth
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.email
import illyan.butler.generated.resources.login
import illyan.butler.generated.resources.password
import illyan.butler.generated.resources.required
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.sign_in_anonymously
import illyan.butler.generated.resources.sign_up
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Login(
    modifier: Modifier = Modifier,
    onSignUp: (String, String) -> Unit,
    onSelectHost: () -> Unit,
    onAuthenticated: () -> Unit
) {
    val viewModel = koinViewModel<LoginViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn == true) onAuthenticated()
    }
    // TODO: implement oath authentication
    Login(
        modifier = modifier.padding(16.dp).imePadding(),
        isUserSigningIn = state.isSigningIn,
        signInAnonymously = null, // TODO: Implement sign in anonymously
        signInWithEmailAndPassword = viewModel::signInWithEmailAndPassword,
        navigateToSignUp = onSignUp,
        selectHost = onSelectHost
    )
}

@Composable
fun Login(
    modifier: Modifier = Modifier,
    isUserSigningIn: Boolean = false,
    signInAnonymously: (() -> Unit)? = null,
    signInWithEmailAndPassword: (email: String, password: String) -> Unit = { _, _ -> },
    navigateToSignUp: (String, String) -> Unit = { _, _ -> },
    selectHost: (() -> Unit)? = null
) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var isEmailBlank by rememberSaveable { mutableStateOf(false) }
    var isPasswordBlank by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier.mediumDialogWidth().width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LoginFields(
            email = email,
            password = password,
            emailChanged = { email = it; isEmailBlank = false },
            passwordChanged = { password = it; isPasswordBlank = false },
            emailError = if (isEmailBlank) { { Text(text = stringResource(Res.string.required)) } } else null,
            passwordError = if (isPasswordBlank) { { Text(text = stringResource(Res.string.required)) } } else null
        )
        LoginButtons(
            signInWithEmailAndPassword = {
                if (email.isBlank()) {
                    isEmailBlank = true
                }
                if (password.isBlank()) {
                    isPasswordBlank = true
                }
                if (email.isNotBlank() && password.isNotBlank()) {
                    signInWithEmailAndPassword(email, password)
                }
            },
            signInAnonymously = signInAnonymously,
            isUserSigningIn = isUserSigningIn,
            navigateToSignUp = { navigateToSignUp(email, password) },
            selectHost = selectHost
        )
    }
}

@Composable
private fun LoginFields(
    modifier: Modifier = Modifier,
    email: String,
    password: String,
    emailChanged: (String) -> Unit = {},
    passwordChanged: (String) -> Unit = {},
    emailError: (@Composable () -> Unit)? = null,
    passwordError: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ButlerTextField(
            modifier = Modifier,
            value = email,
            isOutlined = false,
            enabled = true,
            onValueChange = emailChanged,
            isError = emailError != null,
            supportingText = emailError,
            label = { Text(text = stringResource(Res.string.email)) }
        )
        ButlerTextField(
            modifier = Modifier,
            value = password,
            enabled = true,
            isOutlined = false,
            onValueChange = passwordChanged,
            isError = passwordError != null,
            supportingText = passwordError,
            label = { Text(text = stringResource(Res.string.password)) }
        )
    }
}

@Composable
fun LoginButtons(
    modifier: Modifier = Modifier,
    isUserSigningIn: Boolean = false,
    signInAnonymously: (() -> Unit)? = null,
    signInWithEmailAndPassword: (() -> Unit)? = null,
    navigateToSignUp: (() -> Unit),
    selectHost: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(
            visible = signInWithEmailAndPassword != null
        ) {
            ButlerLargeSolidButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = signInWithEmailAndPassword ?: {},
                enabled = true,
                text = { Text(text = stringResource(Res.string.login)) },
                leadingIcon = if (isUserSigningIn) {
                    {
                        SmallCircularProgressIndicator(
                            modifier = Modifier.animateEnterExit()
                        )
                    }
                } else {
                    null
                },
                trailingIcon = {
                    Icon(imageVector = Icons.AutoMirrored.Rounded.Login, contentDescription = null)
                }
            )
        }

        HorizontalDivider()

        Column(
            modifier = Modifier.width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
    Login(
        isUserSigningIn = false,
        signInAnonymously = {},
        signInWithEmailAndPassword = { _, _ -> },
        navigateToSignUp = { _, _ -> },
        selectHost = {}
    )
}
