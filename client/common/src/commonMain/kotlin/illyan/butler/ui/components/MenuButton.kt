package illyan.butler.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun MenuButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    text: String,
) {
    TextButton(
        modifier = modifier,
        onClick = onClick,
    ) {
        Text(text = text)
        Icon(
            imageVector = Icons.Rounded.ChevronRight, contentDescription = "",
        )
    }
}
