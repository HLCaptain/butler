package illyan.butler.core.ui.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.extendedspans.internal.update
import com.mikepenz.markdown.m3.markdownTypography
import com.mikepenz.markdown.model.rememberMarkdownState
import illyan.butler.core.ui.components.ButlerInvertedStatusMessage
import illyan.butler.core.ui.components.ButlerRichText
import illyan.butler.core.ui.components.ButlerStatusMessage
import illyan.butler.core.ui.components.ButlerStatusMessageDefaults
import illyan.butler.core.ui.components.StatusMessageColors
import illyan.butler.core.ui.theme.ButlerTheme

@Composable
private fun StatusMessagePreview(
    colors: StatusMessageColors,
    border: BorderStroke? = ButlerStatusMessageDefaults.statusMessageBorder(colors)
) {
    ButlerTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ButlerStatusMessage(
                modifier = Modifier.padding(16.dp),
                colors = colors,
                border = border,
                imageVector = Icons.Rounded.Info,
                title = { Text("Status Message Title") },
                description = {
                    val markdownState = rememberMarkdownState { "<p><strong>Butler</strong> by HLCaptain is a Kotlin-based library that offers a clean architecture template for Android and multiplatform apps. It integrates tools like Koin, SqlDelight, and Voyager to simplify scalable app development. For more information, visit the <a href=\"https://github.com/HLCaptain/butler\">GitHub repository</a>.</p>" }
                    ButlerRichText(
                        markdownState = markdownState,
                        typography = markdownTypography(
                            textLink = markdownTypography().textLink.update {
                                copy(color = colors.primaryColor)
                            }
                        )
                    )
                },
                actions = {
                    ButlerStatusMessageDefaults.StatusMessageButtons(
                        colors = colors,
                        primaryButtonText = "Primary Button",
                        onPrimaryClick = {},
                        secondaryButtonText = "Secondary option",
                        onSecondaryClick = {}
                    )
                },
                onClose = {}
            )
        }
    }
}

@Composable
private fun InvertedStatusMessagePreview(
    colors: StatusMessageColors
) {
    ButlerTheme {
        Surface(color = MaterialTheme.colorScheme.surface) {
            ButlerInvertedStatusMessage(
                modifier = Modifier.padding(16.dp),
                colors = colors,
                imageVector = Icons.Rounded.Info,
                title = { Text("Status Message Title") },
                description = {
                    val markdownState = rememberMarkdownState { "<p><strong>Butler</strong> by HLCaptain is a Kotlin-based library that offers a clean architecture template for Android and multiplatform apps. It integrates tools like Koin, SqlDelight, and Voyager to simplify scalable app development. For more information, visit the <a href=\"https://github.com/HLCaptain/butler\">GitHub repository</a>.</p>" }
                    ButlerRichText(
                        markdownState = markdownState,
                        typography = markdownTypography(
                            textLink = markdownTypography().textLink.update {
                                copy(color = colors.primaryColor)
                            }
                        )
                    )
                },
                actions = {
                    ButlerStatusMessageDefaults.InvertedStatusMessageButtons(
                        colors = colors,
                        primaryButtonText = "Primary",
                        onPrimaryClick = {},
                        secondaryButtonText = "Secondary",
                        onSecondaryClick = {}
                    )
                },
                onClose = {}
            )
        }
    }
}

@Preview
@Composable
private fun PrimaryStatusMessagePreview() {
    StatusMessagePreview(ButlerStatusMessageDefaults.statusMessagePrimaryColors())
}

@Preview
@Composable
private fun SecondaryStatusMessagePreview() {
    StatusMessagePreview(ButlerStatusMessageDefaults.statusMessageSecondaryColors())
}

@Preview
@Composable
private fun WarningStatusMessagePreview() {
    StatusMessagePreview(ButlerStatusMessageDefaults.statusMessageWarningColors())
}

@Preview
@Composable
private fun GrayStatusMessagePreview() {
    StatusMessagePreview(ButlerStatusMessageDefaults.statusMessageGrayColors())
}

@Preview
@Composable
private fun PrimaryInvertedStatusMessagePreview() {
    InvertedStatusMessagePreview(ButlerStatusMessageDefaults.invertedStatusMessagePrimaryColors())
}

@Preview
@Composable
private fun SecondaryInvertedStatusMessagePreview() {
    InvertedStatusMessagePreview(ButlerStatusMessageDefaults.invertedStatusMessageSecondaryColors())
}

@Preview
@Composable
private fun WarningInvertedStatusMessagePreview() {
    InvertedStatusMessagePreview(ButlerStatusMessageDefaults.invertedStatusMessageWarningColors())
}

@Preview
@Composable
private fun GrayInvertedStatusMessagePreview() {
    InvertedStatusMessagePreview(ButlerStatusMessageDefaults.invertedStatusMessageGrayColors())
}
