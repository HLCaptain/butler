package illyan.butler.core.ui.preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.theme.ButlerTheme

@PreviewLightDark
@Composable
fun PreviewButlerOutlinedTextField() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = true,
                onValueChange = {},
                label = { Text("Label") },
                placeholder = { Text("Placeholder") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                supportingText = { Text("Supporting text") },
                isError = false
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewButlerOutlinedTextFieldNoLabel() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = true,
                onValueChange = {},
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                supportingText = { Text("Supporting text") },
                isError = false
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewButlerTextField() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = false,
                onValueChange = {},
                label = { Text("Label") },
                placeholder = { Text("Placeholder") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                supportingText = { Text("Supporting text") },
                isError = false
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewButlerTextFieldNoLabel() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                isOutlined = false,
                value = "Text",
                onValueChange = {},
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                supportingText = { Text("Supporting text") },
                isError = false
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewButlerTextFieldError() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = false,
                onValueChange = {},
                label = { Text("Label") },
                placeholder = { Text("Placeholder") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                supportingText = { Text("Supporting text") },
                isError = true
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewButlerOutlinedTextFieldError() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                isOutlined = true,
                value = "Text",
                onValueChange = {},
                label = { Text("Label") },
                placeholder = { Text("Search") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                supportingText = { Text("Supporting text") },
                isError = true
            )
        }
    }
}

@PreviewDynamicColors
@Composable
fun PreviewButlerOutlinedTextFieldDynamicColors() {
    ButlerTheme(dynamicColorEnabled = true) {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = true,
                onValueChange = {},
                label = { Text("Label") },
                placeholder = { Text("Placeholder") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                supportingText = { Text("Supporting text") },
                isError = false
            )
        }
    }
}

@PreviewDynamicColors
@Composable
fun PreviewButlerTextFieldDynamicColors() {
    ButlerTheme(dynamicColorEnabled = true) {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = false,
                onValueChange = {},
                label = { Text("Label") },
                placeholder = { Text("Placeholder") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                supportingText = { Text("Supporting text") },
                isError = false
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewButlerTextFieldCompact() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = false,
                onValueChange = {},
                label = { Text("Label") },
                placeholder = { Text("Placeholder") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                supportingText = { Text("Supporting text") },
                isError = false,
                isCompact = true
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewButlerOutlinedTextFieldCompact() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = true,
                onValueChange = {},
                label = { Text("Label") },
                placeholder = { Text("Placeholder") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Clear text") },
                trailingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Clear text") },
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                supportingText = { Text("Supporting text") },
                isError = false,
                isCompact = true
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewButlerTextFieldCompactNoIcon() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = false,
                onValueChange = {},
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                trailingIcon = null,
                isError = false,
                isCompact = true
            )
        }
    }
}

@PreviewLightDark
@Composable
fun PreviewButlerOutlinedTextFieldCompactNoIcon() {
    ButlerTheme {
        Surface {
            ButlerTextField(
                value = "Text",
                isOutlined = true,
                onValueChange = {},
                prefix = { Text("Prefix") },
                suffix = { Text("Suffix") },
                trailingIcon = null,
                isError = false,
                isCompact = true
            )
        }
    }
}
