package illyan.butler.ui.new_chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel

class NewChatScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<NewChatScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "New Chat Screen")
        }
    }
}