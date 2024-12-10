package illyan.butler.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable (RowScope.() -> Unit)
) {
    ButlerMediumTextButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled
    ) {
        content()
        Icon(
            modifier = Modifier.align(Alignment.CenterVertically),
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = ""
        )
    }
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
