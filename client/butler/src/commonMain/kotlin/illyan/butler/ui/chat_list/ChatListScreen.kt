package illyan.butler.ui.chat_list

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import illyan.butler.domain.model.DomainChat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_chats
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class ChatListScreen(private val selectChat: (String) -> Unit) : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<ChatListScreenModel>()
        val chats by screenModel.userChats.collectAsState()
        ChatList(
            chats = chats,
            openChat = { selectChat(it) }
        )
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun ChatList(
        chats: List<DomainChat>,
        openChat: (uuid: String) -> Unit,
    ) {
        Crossfade(
            modifier = Modifier.padding(8.dp),
            targetState = chats.isEmpty()
        ) {
            if (it) {
                Text(
                    text = stringResource(Res.string.no_chats),
                    style = MaterialTheme.typography.headlineLarge
                )
            } else {
                Column {
                    Text(
                        text = stringResource(Res.string.chats),
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .animateContentSize()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(chats) { chat ->
                                ChatCard(
                                    chat = chat,
                                    openChat = { openChat(chat.id!!) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun ChatCard(
        chat: DomainChat,
        openChat: () -> Unit
    ) {
        Card(
            onClick = openChat
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = chat.name ?: stringResource(Res.string.new_chat),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = chat.id!!.take(16),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}