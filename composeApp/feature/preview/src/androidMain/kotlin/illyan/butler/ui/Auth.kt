package illyan.butler.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import illyan.butler.core.ui.components.ButlerDialogSurface
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.ui.login.LoginDialogContent
import illyan.butler.ui.select_host.SelectHostDialogContent
import illyan.butler.ui.select_host.SelectHostState
import illyan.butler.ui.signup.SignUpDialogContent
import illyan.butler.ui.signup.SignUpScreenState

@PreviewLightDark
@Composable
fun LoginPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            LoginDialogContent(
                isUserSigningIn = false,
                signInAnonymously = {},
                signInWithEmailAndPassword = { _, _ -> },
                navigateToSignUp = { _, _ -> },
                selectHost = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SignUpPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            SignUpDialogContent(
                state = SignUpScreenState(
                    isSignedIn = false,
                    isSigningIn = false
                ),
                initialEmail = "illyan@butler.com",
                initialPassword = "password",
                signUp = { _, _, _ -> }
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SelectHostPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            SelectHostDialogContent(
                state = SelectHostState(
                    isConnecting = false,
                    isConnected = false,
                    currentHost = "http://localhost:8080"
                ),
                testHost = {},
                testAndSelectHost = {}
            )
        }
    }
}
