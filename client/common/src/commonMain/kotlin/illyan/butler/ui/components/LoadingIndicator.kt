package illyan.butler.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import illyan.butler.Res
import illyan.butler.ui.MediumCircularProgressIndicator

@Composable
fun LoadingIndicator(
    progressIndicator: @Composable () -> Unit = {
        MediumCircularProgressIndicator()
    },
    leadingComposable: @Composable () -> Unit = {},
    trailingComposable: @Composable () -> Unit = {
        Text(text = Res.string.loading)
    }
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            leadingComposable()
            progressIndicator()
            trailingComposable()
        }
    }
}