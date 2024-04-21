package illyan.butler.ui.chat_detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainMessage
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.assistant
import illyan.butler.generated.resources.back
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_messages
import illyan.butler.generated.resources.send
import illyan.butler.generated.resources.send_message
import illyan.butler.generated.resources.you
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class ChatDetailScreen(
    private val getSelectedChatId: () -> String?,
    private val onBack: () -> Unit
) : Screen {
    @OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ChatDetailScreenModel>()
        val state by screenModel.state.collectAsState()
        LaunchedEffect(state.chat) { Napier.d("ChatScreen: ${state.chat}") }
        val selectedChatId by remember { derivedStateOf(getSelectedChatId) }
        LaunchedEffect(selectedChatId) {
            Napier.d("SelectedChatId: $selectedChatId")
            selectedChatId?.let { screenModel.loadChat(it) }
        }
        val navigator = LocalNavigator.current
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            state.chat?.name ?: stringResource(Res.string.new_chat),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    navigationIcon = {
                        if (navigator != null && navigator.size > 1) {
                            IconButton(onClick = { onBack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(Res.string.back)
                                )
                            }
                        }
                    },
                    actions = {
//                        IconButton(onClick = { /* do something */ }) {
//                            Icon(
//                                imageVector = Icons.Filled.Menu,
//                                contentDescription = "Localized description"
//                            )
//                        }
                    },
                    scrollBehavior = scrollBehavior,
                )
            },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnimatedVisibility(selectedChatId == null) {
                    SelectChat()
                }
                if (selectedChatId != null) {
                    MessageList(
                        modifier = Modifier.weight(1f, fill = true),
                        chat = state.chat,
                        messages = state.messages ?: emptyList(),
                        userId = state.userId ?: ""
                    )
                    MessageField(sendMessage = screenModel::sendMessage)
                }
            }
        }
    }

    private @Composable
    fun SelectChat() {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Select a chat!")
        }
    }
}



@OptIn(ExperimentalResourceApi::class)
@Composable
fun MessageList(
    modifier: Modifier = Modifier,
    chat: DomainChat?,
    messages: List<DomainMessage> = emptyList(),
    userId: String
) {
    Crossfade(
        modifier = modifier,
        targetState = chat == null || messages.isEmpty()
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (it) {
                Text(
                    text = stringResource(Res.string.no_messages),
                    style = MaterialTheme.typography.headlineLarge
                )
            }
            LazyColumn(
                reverseLayout = true,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                items(messages) { message ->
                    MessageItem(message = message, userId = userId)
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MessageItem(
    message: DomainMessage,
    userId: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = if (message.senderId == userId) Alignment.End else Alignment.Start
    ) {
        Text(
            text = stringResource(if (message.senderId == userId) Res.string.you else Res.string.assistant),
            style = MaterialTheme.typography.labelMedium
        )
        Card {
            Text(
                modifier = Modifier.padding(8.dp),
                text = message.message ?: ""
            )
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun MessageField(
    modifier: Modifier = Modifier,
    sendMessage: (String) -> Unit,
) {
    Row(
        modifier = modifier
            .padding(8.dp)
            .animateContentSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var textMessage by rememberSaveable { mutableStateOf("") }
        OutlinedTextField(
            modifier = Modifier.weight(1f, fill = true),
            value = textMessage,
            onValueChange = { textMessage = it },
            shape = RoundedCornerShape(16.dp),
            placeholder = { Text(stringResource(Res.string.send_message)) }
        )

        IconButton(
            modifier = Modifier.padding(4.dp),
            onClick = {
                sendMessage(textMessage)
                textMessage = ""
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = stringResource(Res.string.send),
                tint =  MaterialTheme.colorScheme.primary
            )
        }
    }
}