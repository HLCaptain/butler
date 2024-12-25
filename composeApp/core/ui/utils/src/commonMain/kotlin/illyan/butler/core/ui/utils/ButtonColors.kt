package illyan.butler.core.ui.utils

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable

@Composable
fun ButtonColors.toCardColors() = CardDefaults.cardColors(
    containerColor = containerColor,
    contentColor = contentColor,
    disabledContainerColor = disabledContainerColor,
    disabledContentColor = disabledContentColor,
)
