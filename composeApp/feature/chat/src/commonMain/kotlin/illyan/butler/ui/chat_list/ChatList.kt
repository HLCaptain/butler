package illyan.butler.ui.chat_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import illyan.butler.domain.model.DomainChat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.delete_chat
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_chats
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ChatList(
    chats: List<DomainChat>,
    openChat: (String) -> Unit,
    deleteChat: (String) -> Unit,
) {
    if (chats.isEmpty()) {
        Text(
            modifier = Modifier.padding(8.dp),
            text = stringResource(Res.string.no_chats),
            style = MaterialTheme.typography.headlineLarge
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chats) { chat ->
                ChatCard(
                    chat = chat,
                    openChat = { openChat(chat.id!!) },
                    deleteChat = { deleteChat(chat.id!!) }
                )
            }
        }
    }
}

@Composable
fun ChatCard(
    chat: DomainChat,
    openChat: () -> Unit,
    deleteChat: () -> Unit,
) {
    Card(
        onClick = openChat,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        )
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
            IconButton(
                onClick = deleteChat,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(Res.string.delete_chat)
                )
            }
        }
    }
}