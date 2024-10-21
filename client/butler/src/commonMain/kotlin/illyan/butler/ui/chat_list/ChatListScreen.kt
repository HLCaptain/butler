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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.model.DomainChat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.delete_chat
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_chats
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun ChatList(
    chats: List<DomainChat>,
    openChat: (uuid: String) -> Unit,
    deleteChat: (uuid: String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val hazeState = remember { HazeState() }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                modifier = Modifier.hazeChild(hazeState),
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(Color.Transparent),
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
            modifier = Modifier
                .padding(insetsPadding)
                .haze(hazeState, HazeMaterials.thin()),
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
                            openChat = { openChat(chat.id!!) },
                            deleteChat = { deleteChat(chat.id!!) }
                        )
                    }
                }
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
    ElevatedCard(onClick = openChat) {
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