package illyan.butler.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import illyan.butler.core.ui.components.ButlerDialogSurface
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.ui.select_host_tutorial.SelectHostTutorial
import illyan.butler.ui.signup_tutorial.SignUpTutorial
import illyan.butler.ui.welcome.Welcome

@Preview
@Composable
fun SelectHostTutorialPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            SelectHostTutorial(
                navigateToHostSelection = {}
            )
        }
    }
}

@Preview
@Composable
fun SignUpTutorialPreview() {
    ButlerTheme {
        ButlerDialogSurface {
            SignUpTutorial(
                onSignUp = {}
            )
        }
    }
}

@Preview
@Composable
fun WelcomePreview() {
    ButlerTheme {
        ButlerDialogSurface {
            Welcome(
                onNext = {}
            )
        }
    }
}