package illyan.butler.ui.chat_list

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import illyan.butler.domain.model.DomainChat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_chats
import illyan.butler.ui.chat_layout.LocalChatSelector
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class ChatListScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ChatListScreenModel>()
        val chats by screenModel.userChats.collectAsState()
        val selectChat = LocalChatSelector.current
        ChatList(
            chats = chats,
            openChat = { selectChat(it) }
        )
    }

    @OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
    @Composable
    fun ChatList(
        chats: List<DomainChat>,
        openChat: (uuid: String) -> Unit,
    ) {
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(Res.string.chats),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { insetsPadding ->
            Crossfade(
                modifier = Modifier.padding(insetsPadding),
                targetState = chats.isEmpty()
            ) {
                if (it) {
                    Text(
                        modifier = Modifier.padding(8.dp),
                        text = stringResource(Res.string.no_chats),
                        style = MaterialTheme.typography.headlineLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.animateContentSize(),
                        contentPadding = PaddingValues(8.dp),
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

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    fun ChatCard(
        chat: DomainChat,
        openChat: () -> Unit
    ) {
        ElevatedCard(
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