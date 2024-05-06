package illyan.butler.ui.chat_details

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.new_chat
import illyan.butler.ui.components.ButlerDialogContent
import illyan.butler.ui.components.smallDialogWidth
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class ChatDetailsScreen : Screen {
    @OptIn(ExperimentalResourceApi::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ChatDetailsScreenModel>()
        val state by screenModel.state.collectAsState()
        ButlerDialogContent(
            modifier = Modifier.smallDialogWidth(),
            title = {
                Text(state.chat?.name ?: stringResource(Res.string.new_chat))
            },
            text = {
                Column {
                    Text("Chat details")
                    Text("Chat name: ${state.chat?.name}")
                    Text("Chat ID: ${state.chat?.id}")
                    Text("AI members: ${state.chat?.members?.filter { it != state.userId }}")
                }
            },
            containerColor = Color.Transparent
        )
    }
}