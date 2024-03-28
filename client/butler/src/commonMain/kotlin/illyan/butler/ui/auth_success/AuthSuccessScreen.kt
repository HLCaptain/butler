package illyan.butler.ui.auth_success

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
        var isGone by rememberSaveable { mutableStateOf(false) }
        val size by animateDpAsState(
            targetValue = if (isGone) 0.dp else 128.dp,
            animationSpec = tween(delayMillis.toInt() / 5)
        )
        LaunchedEffect(Unit) {
            delay(4 * delayMillis / 5)
            isGone = true
            delay(delayMillis / 5)
            close()
        }
        Icon(
            modifier = Modifier.size(size),
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = "Success",
            tint = MaterialTheme.colorScheme.primary
        )
    }
}