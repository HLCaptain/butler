package illyan.butler.ui.chat

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import illyan.butler.Res
import illyan.butler.domain.model.ChatMessage
import illyan.butler.domain.model.DomainChat
import io.github.aakira.napier.Napier
import org.koin.core.parameter.parametersOf

class ChatScreen(private val chatUUID: String) : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<ChatScreenModel> { parametersOf(chatUUID) }
        val chat by screenModel.chat.collectAsState()
        LaunchedEffect(Unit) { Napier.d("ChatScreen: $chat") }
        MessageList(chat = chat)
    }

    @Composable
    fun MessageList(
        chat: DomainChat?,
    ) {
        Crossfade(
            targetState = chat
        ) {
            if (it == null) {
                Text(
                    text = Res.string.no_messages,
                    style = MaterialTheme.typography.headlineLarge
                )
            } else {
                LazyColumn(
                    reverseLayout = true,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    items(it.messages) { message ->
                        MessageItem(message = message)
                    }
                }
            }
        }
    }

    @Composable
    fun MessageItem(
        message: ChatMessage,
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = message.senderUUID,
                style = MaterialTheme.typography.titleLarge
            )
            Card {
                Text(
                    text = message.message,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}