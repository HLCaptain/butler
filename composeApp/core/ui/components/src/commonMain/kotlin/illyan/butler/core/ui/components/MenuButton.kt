package illyan.butler.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable (RowScope.() -> Unit)
) {
    ButlerTextButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        content = {
            ButlerButtonDefaults.ButtonRow(
                rowPadding = ButlerButtonDefaults.SmallRowPadding,
                trailingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null
                    )
                },
                text = content
            )
        }
    )
}

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    text: String
) {
    MenuButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled
    ) {
        Text(
            text = text,
            maxLines = 1
        )
    }
}
