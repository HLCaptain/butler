package illyan.butler.ui.onboard_flow

import androidx.compose.ui.graphics.vector.ImageVector

data class AuthItem(
    val title: String,
    val description: String,
    val image: ImageVector,
    val pros: List<String>,
    val cons: List<String>,
    val enabled: Boolean,
)
