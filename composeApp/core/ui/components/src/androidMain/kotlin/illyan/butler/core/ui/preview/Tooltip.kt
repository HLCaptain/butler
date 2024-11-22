package illyan.butler.core.ui.preview

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import illyan.butler.core.ui.components.TooltipButton

@Preview
@Composable
fun TooltipButtonPreview() {
    TooltipButton(
        onClick = {},
        tooltip = { Text("Tooltip") }
    ) {
        Text("Button")
    }
}