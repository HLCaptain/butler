package illyan.butler.ui.chat_list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.utils.lowerContrastWithBlendTo
import illyan.butler.domain.model.DomainChat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.delete_chat
import illyan.butler.generated.resources.new_chat
import org.jetbrains.compose.resources.stringResource

@Composable
fun ChatList(
    modifier: Modifier = Modifier,
    chats: List<DomainChat>,
    selectedChat: String?,
    openChat: (String) -> Unit,
    deleteChat: (String) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp)
    ) {
        items(chats, key = { it.id!! }) { chat ->
            ChatCard(
                modifier = Modifier.animateItem(),
                chat = chat,
                selected = chat.id == selectedChat,
                openChat = { openChat(chat.id!!) },
                deleteChat = { deleteChat(chat.id!!) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatCard(
    modifier: Modifier = Modifier,
    chat: DomainChat,
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
    Card(
        modifier = modifier,
        onClick = openChat,
        colors = CardDefaults.cardColors(containerColor = cardContainerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = chat.name ?: stringResource(Res.string.new_chat),
                style = MaterialTheme.typography.titleLarge
            )
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
                ExposedDropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    matchTextFieldWidth = false
                ) {
                    DropdownMenuItem(
                        onClick = deleteChat,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(Res.string.delete_chat)) }
                    )
                }
            }
        }
    }
}