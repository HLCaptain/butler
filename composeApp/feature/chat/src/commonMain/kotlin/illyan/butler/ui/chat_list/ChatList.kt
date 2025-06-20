package illyan.butler.ui.chat_list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerDropdownMenu
import illyan.butler.core.ui.components.ButlerDropdownMenuDefaults
import illyan.butler.core.ui.utils.lowerContrastWithBlendTo
import illyan.butler.domain.model.Chat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.delete_chat
import illyan.butler.generated.resources.new_chat
import org.jetbrains.compose.resources.stringResource
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    chats: List<Chat>,
    selectedChat: Uuid?,
    openChat: (Uuid) -> Unit,
    deleteChat: (Chat) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
    ) {
        items(chats, key = { it.id }) { chat ->
            ChatListItemCard(
                modifier = Modifier.animateItem(),
                chat = chat,
                selected = chat.id == selectedChat,
                openChat = { openChat(chat.id) },
                deleteChat = { deleteChat(chat) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListItemCard(
    modifier: Modifier = Modifier,
    chat: Chat,
    selected: Boolean,
    openChat: () -> Unit,
    deleteChat: () -> Unit,
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    val backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(LocalAbsoluteTonalElevation.current)
    val cardContainerColor by animateColorAsState(
        if (selected) {
            MaterialTheme.colorScheme.primaryContainer.lowerContrastWithBlendTo(backgroundColor, 1.15)
        } else {
            backgroundColor
        }
    )
    ButlerCard(
        modifier = modifier,
        onClick = openChat,
        colors = ButlerCardDefaults.cardColors(containerColor = cardContainerColor),
        contentPadding = PaddingValues(top = 4.dp, bottom = 4.dp, start = 8.dp, end = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val chatName = chat.title ?: stringResource(Res.string.new_chat)
                val style = when (chatName.length) {
                    in 0..10 -> MaterialTheme.typography.titleLarge
                    in 11..20 -> MaterialTheme.typography.titleMedium
                    else -> MaterialTheme.typography.titleSmall
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = chatName,
                    style = style,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                AnimatedVisibility(visible = chat.deviceOnly) {
                    Icon(
                        imageVector = Icons.Rounded.CloudOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            ExposedDropdownMenuBox(
                expanded = showMenu,
                onExpandedChange = { showMenu = it }
            ) {
                IconToggleButton(
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    checked = showMenu,
                    onCheckedChange = { showMenu = it },
                    colors = IconButtonDefaults.iconToggleButtonColors().copy(
                        checkedContentColor = MaterialTheme.colorScheme.primary,
                        contentColor = LocalContentColor.current
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreHoriz,
                        contentDescription = null
                    )
                }
                ButlerDropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    matchTextFieldWidth = false,
                ) {
                    ButlerDropdownMenuDefaults.DropdownMenuItem(
                        onClick = { showMenu = false; deleteChat() },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null
                            )
                        },
                        content = { Text(stringResource(Res.string.delete_chat)) },
                        colors = ButlerCardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(16.dp)
                        )
                    )
                }
            }
        }
    }
}
