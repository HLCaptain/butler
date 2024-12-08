package illyan.butler.ui.chat_details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import illyan.butler.core.ui.components.ButlerDialogContent
import illyan.butler.core.ui.components.mediumDialogSize
import illyan.butler.domain.model.DomainChat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.ai_members
import illyan.butler.generated.resources.chat_details
import illyan.butler.generated.resources.chat_id_is_x
import illyan.butler.generated.resources.chat_name_is_x
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.none
import illyan.butler.generated.resources.unknown
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatDetails(
    modifier: Modifier = Modifier,
    chatId: String?,
    actions: @Composable () -> Unit = {}
) {
    val screenModel = koinViewModel<ChatDetailsViewModel>()
    val state by screenModel.state.collectAsState()
    LaunchedEffect(Unit) {
        screenModel.loadChat(chatId)
    }
    ChatDetails(
        modifier = modifier,
        chat = state.chat,
        currentUserId = state.userId,
        actions = actions
    )
}

@Composable
fun ChatDetails(
    modifier: Modifier = Modifier,
    chat: DomainChat?,
    currentUserId: String?,
    actions: @Composable () -> Unit = {},
) {
    Box {
        ButlerDialogContent(
            modifier = modifier.mediumDialogSize(),
            title = {
                Text(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    text = stringResource(Res.string.chat_details)
                )
            },
            text = {
                val aiMembers = chat?.members?.filter { it != currentUserId } ?: emptyList()
                val aiMembersString = if (aiMembers.isEmpty()) {
                    stringResource(Res.string.none)
                } else {
                    aiMembers.joinToString(", ")
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(Res.string.chat_name_is_x).format(chat?.name ?: stringResource(Res.string.new_chat)))
                    Text(stringResource(Res.string.chat_id_is_x).format(chat?.id ?: stringResource(Res.string.unknown)))
                    Text(stringResource(Res.string.ai_members).format(aiMembersString))
                    chat?.summary?.let { Text(it) }
                }
            },
            containerColor = Color.Transparent
        )
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            actions()
        }
    }
}