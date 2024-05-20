package illyan.butler.ui.chat_details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.ai_members
import illyan.butler.generated.resources.chat_details
import illyan.butler.generated.resources.chat_id_is_x
import illyan.butler.generated.resources.chat_name_is_x
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.none
import illyan.butler.generated.resources.unknown
import illyan.butler.ui.components.ButlerDialogContent
import illyan.butler.ui.components.mediumDialogSize
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class ChatDetailsScreen(private val chatId: String?) : Screen {
    @OptIn(ExperimentalResourceApi::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ChatDetailsScreenModel>()
        val state by screenModel.state.collectAsState()
        LaunchedEffect(Unit) {
            screenModel.loadChat(chatId)
        }
        ButlerDialogContent(
            modifier = Modifier.mediumDialogSize(),
            title = {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(Res.string.chat_details)
                )
            },
            text = {
                val aiMembers = state.chat?.members?.filter { it != state.userId } ?: emptyList()
                val aiMembersString = if (aiMembers.isEmpty()) {
                    stringResource(Res.string.none)
                } else {
                    aiMembers.joinToString(", ")
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(Res.string.chat_name_is_x).format(state.chat?.name ?: stringResource(Res.string.new_chat)))
                    Text(stringResource(Res.string.chat_id_is_x).format(state.chat?.id ?: stringResource(Res.string.unknown)))
                    Text(stringResource(Res.string.ai_members).format(aiMembersString))
                    state.chat?.summary?.let { Text(it) }
                }
            },
            containerColor = Color.Transparent
        )
    }
}