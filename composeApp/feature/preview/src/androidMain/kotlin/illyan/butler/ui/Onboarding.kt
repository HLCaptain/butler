package illyan.butler.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.ui.select_host.SelectHostState
import illyan.butler.ui.select_host_tutorial.SelectHostTutorial
import illyan.butler.ui.select_host_tutorial.SelectHostTutorialContent
import illyan.butler.ui.signup_tutorial.SignUpTutorial
import illyan.butler.ui.welcome.Welcome

@PreviewLightDark
@Composable
fun SelectHostTutorialPreview() {
    ButlerTheme {
        Surface {
            SelectHostTutorialContent(
                state = SelectHostState(),
                testAndSelectHost = {},
                testHost = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
fun SignUpTutorialPreview() {
    ButlerTheme {
        Surface {
            SignUpTutorial(
                authSuccessEnded = {}
            )
        }
    }
}

@PreviewLightDark
@Composable
fun WelcomePreview() {
    ButlerTheme {
        Surface {
            Welcome(
                onNext = {}
            )
        }
    }
}
