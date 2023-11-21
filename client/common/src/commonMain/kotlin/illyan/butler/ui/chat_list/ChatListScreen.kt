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
import androidx.compose.material3.ExperimentalMaterial3Api
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
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.Res
import illyan.butler.domain.model.DomainChat
import illyan.butler.ui.chat.ChatScreen

class ChatListScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<ChatListScreenModel>()
        val chatsPerModel by screenModel.userChatsPerModel.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        ChatList(
            chatsPerModel = chatsPerModel,
            openChat = { navigator.push(ChatScreen(it)) }
        )
    }

    @Composable
    fun ChatList(
        chatsPerModel: Map<String, List<DomainChat>>,
        openChat: (uuid: String) -> Unit,
    ) {
        Crossfade(
            modifier = Modifier.animateContentSize(),
            targetState = chatsPerModel.isEmpty()
        ) {
            if (it) {
                Text(
                    text = Res.string.no_chats,
                    style = MaterialTheme.typography.headlineLarge
                )
            } else {
                Column {
                    Text(
                        text = Res.string.chats,
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Column(
                        modifier = Modifier
                            .animateContentSize()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        chatsPerModel.keys.toList().forEach { key ->
                            Text(
                                text = key.take(16),
                                style = MaterialTheme.typography.titleMedium
                            )
                            LazyColumn(
                                modifier = Modifier
                                    .animateContentSize()
                                    .padding(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(chatsPerModel[key]!!) { chat ->
                                    ChatCard(
                                        chat = chat,
                                        openChat = { openChat(chat.uuid) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
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
                        text = chat.uuid.take(16),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = chat.uuid.take(16),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}