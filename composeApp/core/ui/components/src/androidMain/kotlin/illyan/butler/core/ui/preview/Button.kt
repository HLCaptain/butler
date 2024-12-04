package illyan.butler.core.ui.preview

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.ButlerButtonDefaults
import illyan.butler.core.ui.components.ButlerLargeOutlinedButton
import illyan.butler.core.ui.components.ButlerLargeSolidButton
import illyan.butler.core.ui.components.ButlerLargeTextButton
import illyan.butler.core.ui.components.ButlerMediumOutlinedButton
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerMediumTextButton
import illyan.butler.core.ui.components.ButlerSmallOutlinedButton
import illyan.butler.core.ui.components.ButlerSmallSolidButton
import illyan.butler.core.ui.components.ButlerSmallTextButton
import illyan.butler.core.ui.theme.ButlerTheme

@Composable
private fun ButlerSmallButtonPreviews(
    solidButtonColors: ButtonColors,
    outlinedButtonColors: ButtonColors,
    outlinedBorderStroke: BorderStroke,
    textButtonColors: ButtonColors,
    enabled: Boolean = true
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ButlerSmallSolidButton(
                onClick = {},
                colors = solidButtonColors,
                text = { Text("Small") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerSmallOutlinedButton(
                onClick = {},
                colors = outlinedButtonColors,
                border = outlinedBorderStroke,
                text = { Text("Small") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerSmallTextButton(
                onClick = {},
                colors = textButtonColors,
                text = { Text("Small") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
        }
    }

}

@Composable
fun ButlerSmallInvertedButtonPreviews(
    invertedSolidButtonColors: ButtonColors,
    invertedOutlinedButtonColors: ButtonColors,
    invertedOutlinedBorderStroke: BorderStroke,
    invertedTextButtonColors: ButtonColors,
    enabled: Boolean = true
) {
    Surface(color = invertedSolidButtonColors.contentColor) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ButlerSmallSolidButton(
                onClick = {},
                colors = invertedSolidButtonColors,
                text = { Text("Small") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerSmallOutlinedButton(
                onClick = {},
                colors = invertedOutlinedButtonColors,
                border = invertedOutlinedBorderStroke,
                text = { Text("Small") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerSmallTextButton(
                onClick = {},
                colors = invertedTextButtonColors,
                text = { Text("Small") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
        }
    }
}

@Composable
fun ButlerMediumButtonPreviews(
    solidButtonColors: ButtonColors,
    outlinedButtonColors: ButtonColors,
    outlinedBorderStroke: BorderStroke,
    textButtonColors: ButtonColors,
    enabled: Boolean = true
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ButlerMediumSolidButton(
                onClick = {},
                colors = solidButtonColors,
                text = { Text("Medium") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerMediumOutlinedButton(
                onClick = {},
                colors = outlinedButtonColors,
                border = outlinedBorderStroke,
                text = { Text("Medium") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerMediumTextButton(
                onClick = {},
                colors = textButtonColors,
                text = { Text("Medium") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
        }
    }

}

@Composable
fun ButlerMediumInvertedButtonPreviews(
    invertedSolidButtonColors: ButtonColors,
    invertedOutlinedButtonColors: ButtonColors,
    invertedOutlinedBorderStroke: BorderStroke,
    invertedTextButtonColors: ButtonColors,
    enabled: Boolean = true
) {
    Surface(color = invertedSolidButtonColors.contentColor) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ButlerMediumSolidButton(
                onClick = {},
                colors = invertedSolidButtonColors,
                text = { Text("Medium") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerMediumOutlinedButton(
                onClick = {},
                colors = invertedOutlinedButtonColors,
                border = invertedOutlinedBorderStroke,
                text = { Text("Medium") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerMediumTextButton(
                onClick = {},
                colors = invertedTextButtonColors,
                text = { Text("Medium") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
        }
    }
}

@Composable
fun ButlerLargeButtonPreviews(
    solidButtonColors: ButtonColors,
    outlinedButtonColors: ButtonColors,
    outlinedBorderStroke: BorderStroke,
    textButtonColors: ButtonColors,
    enabled: Boolean = true
) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ButlerLargeSolidButton(
                onClick = {},
                colors = solidButtonColors,
                text = { Text("Large") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerLargeOutlinedButton(
                onClick = {},
                colors = outlinedButtonColors,
                border = outlinedBorderStroke,
                text = { Text("Large") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerLargeTextButton(
                onClick = {},
                colors = textButtonColors,
                text = { Text("Large") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
        }
    }
}

@Composable
private fun ButlerLargeInvertedButtonPreviews(
    invertedSolidButtonColors: ButtonColors,
    invertedOutlinedButtonColors: ButtonColors,
    invertedOutlinedBorderStroke: BorderStroke,
    invertedTextButtonColors: ButtonColors,
    enabled: Boolean = true
) {
    Surface(color = invertedSolidButtonColors.contentColor) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ButlerLargeSolidButton(
                onClick = {},
                colors = invertedSolidButtonColors,
                text = { Text("Large") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerLargeOutlinedButton(
                onClick = {},
                colors = invertedOutlinedButtonColors,
                border = invertedOutlinedBorderStroke,
                text = { Text("Large") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
            ButlerLargeTextButton(
                onClick = {},
                colors = invertedTextButtonColors,
                text = { Text("Large") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                trailingIcon = { Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null) },
                enabled = enabled
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerSmallPrimaryButtonPreviews() {
    ButlerTheme {
        Row {
            Column {
                ButlerSmallButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonPrimaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryBorder(true),
                    textButtonColors = ButlerButtonDefaults.textButtonPrimaryColors()
                )
                ButlerSmallButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonPrimaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryBorder(false),
                    textButtonColors = ButlerButtonDefaults.textButtonPrimaryColors(),
                    enabled = false
                )
            }
            Column {
                ButlerSmallInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonPrimaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryInvertedBorder(true),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonPrimaryInvertedColors()
                )
                ButlerSmallInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonPrimaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryInvertedBorder(false),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonPrimaryInvertedColors(),
                    enabled = false
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerMediumPrimaryButtonPreviews() {
    ButlerTheme {
        Row {
            Column {
                ButlerMediumButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonPrimaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryBorder(true),
                    textButtonColors = ButlerButtonDefaults.textButtonPrimaryColors()
                )
                ButlerMediumButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonPrimaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryBorder(false),
                    textButtonColors = ButlerButtonDefaults.textButtonPrimaryColors(),
                    enabled = false
                )
            }
            Column {
                ButlerMediumInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonPrimaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryInvertedBorder(true),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonPrimaryInvertedColors()
                )
                ButlerMediumInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonPrimaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryInvertedBorder(false),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonPrimaryInvertedColors(),
                    enabled = false
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerLargePrimaryButtonPreviews() {
    ButlerTheme {
        Row {
            Column {
                ButlerLargeButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonPrimaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryBorder(true),
                    textButtonColors = ButlerButtonDefaults.textButtonPrimaryColors()
                )
                ButlerLargeButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonPrimaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryBorder(false),
                    textButtonColors = ButlerButtonDefaults.textButtonPrimaryColors(),
                    enabled = false
                )
            }
            Column {
                ButlerLargeInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonPrimaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryInvertedBorder(true),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonPrimaryInvertedColors()
                )
                ButlerLargeInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonPrimaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonPrimaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonPrimaryInvertedBorder(false),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonPrimaryInvertedColors(),
                    enabled = false
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerSmallSecondaryButtonPreviews() {
    ButlerTheme {
        Row {
            Column {
                ButlerSmallButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonSecondaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryBorder(true),
                    textButtonColors = ButlerButtonDefaults.textButtonSecondaryColors()
                )
                ButlerSmallButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonSecondaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryBorder(false),
                    textButtonColors = ButlerButtonDefaults.textButtonSecondaryColors(),
                    enabled = false
                )
            }
            Column {
                ButlerSmallInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonSecondaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryInvertedBorder(true),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonSecondaryInvertedColors()
                )
                ButlerSmallInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonSecondaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryInvertedBorder(false),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonSecondaryInvertedColors(),
                    enabled = false
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerMediumSecondaryButtonPreviews() {
    ButlerTheme {
        Row {
            Column {
                ButlerMediumButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonSecondaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryBorder(true),
                    textButtonColors = ButlerButtonDefaults.textButtonSecondaryColors()
                )
                ButlerMediumButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonSecondaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryBorder(false),
                    textButtonColors = ButlerButtonDefaults.textButtonSecondaryColors(),
                    enabled = false
                )
            }
            Column {
                ButlerMediumInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonSecondaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryInvertedBorder(true),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonSecondaryInvertedColors()
                )
                ButlerMediumInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonSecondaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryInvertedBorder(false),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonSecondaryInvertedColors(),
                    enabled = false
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerLargeSecondaryButtonPreviews() {
    ButlerTheme {
        Row {
            Column {
                ButlerLargeButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonSecondaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryBorder(true),
                    textButtonColors = ButlerButtonDefaults.textButtonSecondaryColors()
                )
                ButlerLargeButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonSecondaryColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryBorder(false),
                    textButtonColors = ButlerButtonDefaults.textButtonSecondaryColors(),
                    enabled = false
                )
            }
            Column {
                ButlerLargeInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonSecondaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryInvertedBorder(true),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonSecondaryInvertedColors()
                )
                ButlerLargeInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonSecondaryInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonSecondaryInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonSecondaryInvertedBorder(false),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonSecondaryInvertedColors(),
                    enabled = false
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerSmallGrayButtonPreviews() {
    ButlerTheme {
        Row {
            Column {
                ButlerSmallButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonGrayColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayBorder(true),
                    textButtonColors = ButlerButtonDefaults.textButtonGrayColors()
                )
                ButlerSmallButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonGrayColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayBorder(false),
                    textButtonColors = ButlerButtonDefaults.textButtonGrayColors(),
                    enabled = false
                )
            }
            Column {
                ButlerSmallInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonGrayInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayInvertedBorder(true),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonGrayInvertedColors()
                )
                ButlerSmallInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonGrayInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayInvertedBorder(false),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonGrayInvertedColors(),
                    enabled = false
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerMediumGrayButtonPreviews() {
    ButlerTheme {
        Row {
            Column {
                ButlerMediumButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonGrayColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayBorder(true),
                    textButtonColors = ButlerButtonDefaults.textButtonGrayColors()
                )
                ButlerMediumButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonGrayColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayBorder(false),
                    textButtonColors = ButlerButtonDefaults.textButtonGrayColors(),
                    enabled = false
                )
            }
            Column {
                ButlerMediumInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonGrayInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayInvertedBorder(true),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonGrayInvertedColors()
                )
                ButlerMediumInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonGrayInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayInvertedBorder(false),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonGrayInvertedColors(),
                    enabled = false
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ButlerLargeGrayButtonPreviews() {
    ButlerTheme {
        Row {
            Column {
                ButlerLargeButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonGrayColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayBorder(true),
                    textButtonColors = ButlerButtonDefaults.textButtonGrayColors()
                )
                ButlerLargeButtonPreviews(
                    solidButtonColors = ButlerButtonDefaults.solidButtonGrayColors(),
                    outlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayColors(),
                    outlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayBorder(false),
                    textButtonColors = ButlerButtonDefaults.textButtonGrayColors(),
                    enabled = false
                )
            }
            Column {
                ButlerLargeInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonGrayInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayInvertedBorder(true),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonGrayInvertedColors()
                )
                ButlerLargeInvertedButtonPreviews(
                    invertedSolidButtonColors = ButlerButtonDefaults.solidButtonGrayInvertedColors(),
                    invertedOutlinedButtonColors = ButlerButtonDefaults.outlinedButtonGrayInvertedColors(),
                    invertedOutlinedBorderStroke = ButlerButtonDefaults.outlinedButtonGrayInvertedBorder(false),
                    invertedTextButtonColors = ButlerButtonDefaults.textButtonGrayInvertedColors(),
                    enabled = false
                )
            }
        }
    }
}
