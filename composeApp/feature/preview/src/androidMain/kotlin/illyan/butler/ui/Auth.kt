package illyan.butler.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import illyan.butler.core.ui.components.ButlerDialogSurface
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.ui.apikey.ApiKeyCredentialList
import illyan.butler.ui.server.login.LoginDialogContent
import illyan.butler.ui.server.select_host.SelectHostDialogContent
import illyan.butler.ui.server.select_host.SelectHostState
import illyan.butler.ui.server.signup.SignUpDialogContent
import illyan.butler.ui.server.signup.SignUpScreenState

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
                signUp = { _, _ -> }
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

@OptIn(ExperimentalSharedTransitionApi::class)
@PreviewLightDark
@Composable
fun ApiKeyCredentialListPreview() {
    SharedTransitionLayout {
        AnimatedContent(true) {
            if (it) {
                ButlerTheme {
                    Surface {
                        ApiKeyCredentialList(
                            credentials = emptyList(),
                            editCredential = {},
                            deleteCredential = {},
                            sharedTransitionScope = this@SharedTransitionLayout,
                            animationScope = this@AnimatedContent,
                            createNewCredential = {}
                        )
                    }
                }
            }
        }
    }
}
