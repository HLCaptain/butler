package illyan.butler.ui.chat_layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.core.ui.utils.BackHandler
import illyan.butler.core.ui.utils.ReverseLayoutDirection
import illyan.butler.ui.chat_detail.ChatDetail
import illyan.butler.ui.chat_detail.ChatDetailViewModel
import illyan.butler.ui.chat_details.ChatDetails
import illyan.butler.ui.new_chat.NewChat
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun ChatLayout(
    currentChat: String?,
    selectChat: (String?) -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    var isChatDetailsOpen by rememberSaveable(currentChat) { mutableStateOf(false) }
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val compact = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    BackHandler(isChatDetailsOpen) {
        isChatDetailsOpen = false
    }
    LaunchedEffect(currentChat) {
        if (currentChat == null) {
            isChatDetailsOpen = false
        }
    }
    LaunchedEffect(drawerState.isOpen) {
        // Closed by gesture in compact mode
        if (compact && !drawerState.isOpen) {
            isChatDetailsOpen = false
        }
    }
    LaunchedEffect(compact, isChatDetailsOpen) {
        if (compact && isChatDetailsOpen) drawerState.open() else drawerState.close()
    }
    ReverseLayoutDirection {
        CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                        drawerContentColor = MaterialTheme.colorScheme.onSurface
                    ) {
                        ReverseLayoutDirection {
                            Box(modifier = Modifier.fillMaxHeight()) {
                                ChatDetails(
                                    chatId = currentChat,
                                    actions = {
                                        IconButton(
                                            modifier = Modifier.padding(end = 4.dp, top = 8.dp),
                                            onClick = { isChatDetailsOpen = false }
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = null)
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                gesturesEnabled = compact && drawerState.isOpen
            ) {
                ReverseLayoutDirection {
                    val viewModel = koinViewModel<ChatDetailViewModel>()
                    val state by viewModel.state.collectAsState()
                    LaunchedEffect(currentChat) {
                        currentChat?.let { viewModel.loadChat(it) }
                    }
                    Row {
                        AnimatedContent(
                            modifier = Modifier.weight(1f),
                            targetState = currentChat != null
                        ) { chatSelected ->
                            if (chatSelected) {
                                ChatDetail(
                                    state = state,
                                    sendMessage = viewModel::sendMessage,
                                    toggleRecord = viewModel::toggleRecording,
                                    sendImage = viewModel::sendImage,
                                    playAudio = viewModel::playAudio,
                                    stopAudio = viewModel::stopAudio,
                                    navigationIcon = navigationIcon,
                                    openChatDetails = { isChatDetailsOpen = !isChatDetailsOpen },
                                    isChatDetailsOpen = isChatDetailsOpen
                                )
                            } else {
                                NewChat(
                                    selectNewChat = selectChat,
                                    navigationIcon = navigationIcon
                                )
                            }
                        }
                        AnimatedVisibility(
                            visible = !compact && isChatDetailsOpen,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            PermanentDrawerSheet(
                                drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                                drawerContentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                Box(modifier = Modifier.fillMaxHeight()) {
                                    ChatDetails(
                                        chatId = currentChat,
                                        actions = {
                                            IconButton(
                                                modifier = Modifier.padding(end = 4.dp, top = 8.dp),
                                                onClick = { isChatDetailsOpen = false }
                                            ) {
                                                Icon(Icons.Rounded.Close, contentDescription = null)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
