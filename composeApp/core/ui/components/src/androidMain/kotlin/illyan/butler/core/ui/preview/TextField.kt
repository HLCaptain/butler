package illyan.butler.core.ui.preview

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
