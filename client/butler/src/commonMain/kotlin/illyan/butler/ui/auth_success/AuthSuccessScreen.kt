package illyan.butler.ui.auth_success

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.delay

class AuthSuccessScreen(
    private val delayMillis: Long = 1000,
    private val close: () -> Unit
) : Screen {
    @Composable
    override fun Content() {
        // Big checkmark for 1 second, then close
        LaunchedEffect(Unit) {
            delay(delayMillis)
            close()
        }
        Icon(
            modifier = Modifier.size(128.dp),
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = "Success",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}