package illyan.butler.ui.chat_layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
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
    modifier: Modifier = Modifier,
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
    var drawerContentWidthInPixels by remember { mutableStateOf(0) }
    var permanentDrawerWidthInPixels by remember { mutableStateOf(0) }
    val drawerOpenRatio = remember(
        compact,
        drawerState.currentOffset,
        drawerContentWidthInPixels,
        permanentDrawerWidthInPixels
    ) {
        ((if (compact)
                (drawerState.currentOffset / drawerContentWidthInPixels) + 1
        else {
            permanentDrawerWidthInPixels / drawerContentWidthInPixels.toFloat()
        }).takeIf { !it.isNaN() } ?: 0f).coerceIn(0f, 1f)
    }
    ReverseLayoutDirection {
        CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
            DismissibleNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DismissibleDrawerSheet(
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                        drawerContentColor = MaterialTheme.colorScheme.onSurface,
                        drawerShape = RectangleShape,
                    ) {
                        ReverseLayoutDirection {
                            Box(modifier = Modifier.fillMaxHeight()) {
                                ChatDetails(
                                    modifier = Modifier.onSizeChanged {
                                        drawerContentWidthInPixels = it.width
                                    },
                                    chatId = currentChat,
                                    actions = {
                                        IconButton(
                                            modifier = Modifier.padding(end = 4.dp, top = 8.dp),
                                            onClick = { isChatDetailsOpen = false }
                                        ) {
                                            Icon(Icons.Rounded.Close, contentDescription = null)
                                        }
                                    },
                                )
                            }
                        }
                    }
                },
                gesturesEnabled = isChatDetailsOpen && compact
            ) {
                ReverseLayoutDirection {
                    val viewModel = koinViewModel<ChatDetailViewModel>()
                    val state by viewModel.state.collectAsState()
                    LaunchedEffect(currentChat) {
                        currentChat?.let { viewModel.loadChat(it) }
                    }
                    Row(modifier = modifier) {
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
                                    isChatDetailsOpenRatio = drawerOpenRatio
                                )
                            } else {
                                NewChat(
                                    selectNewChat = selectChat,
                                    navigationIcon = navigationIcon
                                )
                            }
                        }
                        AnimatedVisibility(
                            modifier = Modifier.onSizeChanged {
                                permanentDrawerWidthInPixels = it.width
                            },
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
                                        modifier = Modifier.onSizeChanged {
                                            drawerContentWidthInPixels = it.width
                                        },
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
                    // Area to open and close the chat details if not in compact mode
                    // Capture gestures to open and close the chat details
//                    var dragStartX by remember { mutableStateOf(-1f) }
//                    val density = LocalDensity.current
//                    val dragThreshold = 24.dp
//                    Box(
//                        modifier = Modifier
//                            .fillMaxHeight()
//                            .offset(x = 12.dp)
//                            .width(64.dp)
//                            .onDrag(
//                                enabled = !compact,
//                                onDragStart = { dragStartX = it.x },
//                                onDrag = { offset ->
//                                    if (dragStartX != -1f) {
//                                        with(density) {
//                                            val delta = offset.x - dragStartX
//                                            if (delta > dragThreshold.toPx()) {
//                                                isChatDetailsOpen = true
//                                            } else if (delta < -dragThreshold.toPx()) {
//                                                isChatDetailsOpen = false
//                                            }
//                                        }
//                                    }
//                                },
//                                onDragEnd = { dragStartX = -1f },
//                                onDragCancel = { dragStartX = -1f }
//                        )
//                    )
                }
            }
        }
    }
}
