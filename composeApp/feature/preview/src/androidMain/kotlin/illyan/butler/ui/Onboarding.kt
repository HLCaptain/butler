package illyan.butler.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import illyan.butler.core.ui.theme.ButlerTheme
import illyan.butler.ui.onboard_flow.OnboardFlow

@PreviewLightDark
@PreviewScreenSizes
@Composable
fun SelectHostTutorialPreview() {
    ButlerTheme {
        Surface {
            OnboardFlow(
                authSuccessEnded = {},
            )
        }
    }
}
