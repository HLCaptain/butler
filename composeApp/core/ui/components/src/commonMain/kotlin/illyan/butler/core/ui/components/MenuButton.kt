package illyan.butler.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MediumMenuButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable (RowScope.() -> Unit)
) {
    ButlerMediumTextButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        text = content,
        trailingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null
            )
        },
    )
}

@Composable
fun SmallMenuButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    content: @Composable (RowScope.() -> Unit)
) {
    ButlerSmallTextButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        text = content,
        trailingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null
            )
        },
    )
}

@Composable
fun MediumMenuButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    text: String
) {
    MediumMenuButton(
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

@Composable
fun SmallMenuButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    text: String
) {
    SmallMenuButton(
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
