package illyan.butler.ui.chat_layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
import kotlinx.serialization.Serializable
import org.koin.compose.viewmodel.koinViewModel

sealed class ChatLayoutDestinations {
    @Serializable
    data object NewChat : ChatLayoutDestinations()
    @Serializable
    data class ChatDetail(val chatId: String) : ChatLayoutDestinations()
}

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
    val notExpanded = windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.EXPANDED
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    BackHandler(isChatDetailsOpen) {
        isChatDetailsOpen = false
    }
    LaunchedEffect(drawerState.isOpen) {
        // Closed by gesture in compact mode
        if (notExpanded && !drawerState.isOpen) {
            isChatDetailsOpen = false
        }
    }
    LaunchedEffect(isChatDetailsOpen) {
        if (isChatDetailsOpen) {
            drawerState.open()
        } else {
            drawerState.close()
        }
    }

    LaunchedEffect(currentChat) {
        if (currentChat == null) {
            isChatDetailsOpen = false
        }
    }
    var drawerContentWidthInPixels by remember { mutableStateOf(0) }
    var permanentDrawerWidthInPixels by remember { mutableStateOf(0) }
    val drawerOpenRatio = remember(
        notExpanded,
        drawerState.currentOffset,
        drawerContentWidthInPixels,
        permanentDrawerWidthInPixels
    ) {
        ((if (notExpanded)
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
                        drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation((0.2).dp),
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
                gesturesEnabled = isChatDetailsOpen && notExpanded
            ) {
                ReverseLayoutDirection {
                    val viewModel = koinViewModel<ChatDetailViewModel>()
                    val state by viewModel.state.collectAsState()
                    val navController = rememberNavController()
                    LaunchedEffect(currentChat) {
                        currentChat?.let { viewModel.loadChat(it) }
                        if (navController.currentDestination?.hasRoute<ChatLayoutDestinations.ChatDetail>() != true && currentChat != null) {
                            
                            navController.navigate(ChatLayoutDestinations.ChatDetail(currentChat)) {
                                restoreState = true
                            }
                        } else if (navController.currentDestination?.hasRoute<ChatLayoutDestinations.NewChat>() != true && currentChat == null) {
                            navController.navigate(ChatLayoutDestinations.NewChat) {
                                restoreState = true
                            }
                        }
                    }
                    Row(modifier = modifier) {
                        Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation((0.2).dp)) {
                            NavHost(
                                modifier = Modifier.weight(1f).clip(
                                    RoundedCornerShape(
                                        topEnd = 24.dp * drawerOpenRatio,
                                        bottomEnd = 24.dp * drawerOpenRatio,
                                    )
                                ),
                                navController = navController,
                                startDestination = ChatLayoutDestinations.NewChat,
                            ) {
                                composable<ChatLayoutDestinations.NewChat> {
                                    NewChat(
                                        selectNewChat = selectChat,
                                        navigationIcon = navigationIcon
                                    )
                                }
                                composable<ChatLayoutDestinations.ChatDetail> {
                                    ChatDetail(
                                        state = state,
                                        sendMessage = viewModel::sendMessage,
                                        toggleRecord = viewModel::toggleRecording,
                                        sendImage = viewModel::sendImage,
                                        playAudio = viewModel::playAudio,
                                        stopAudio = viewModel::stopAudio,
                                        navigationIcon = navigationIcon,
                                        toggleChatDetails = { isChatDetailsOpen = !isChatDetailsOpen },
                                        isChatDetailsOpenRatio = drawerOpenRatio,
                                        refreshChat = viewModel::refreshChat,
                                        sendError = viewModel::sendError
                                    )
                                }
                            }
                            AnimatedContent(
                                modifier = Modifier.weight(1f).clip(
                                    RoundedCornerShape(
                                        topEnd = 24.dp * drawerOpenRatio,
                                        bottomEnd = 24.dp * drawerOpenRatio,
                                    )
                                ),
                                targetState = currentChat != null,
                                transitionSpec = {
                                    fadeIn(tween(200)) togetherWith fadeOut(tween(200)) using SizeTransform(clip = false) { _, _ -> tween(0) }
                                }
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
                                        toggleChatDetails = { isChatDetailsOpen = !isChatDetailsOpen },
                                        isChatDetailsOpenRatio = drawerOpenRatio,
                                        refreshChat = viewModel::refreshChat,
                                        sendError = viewModel::sendError
                                    )
                                } else {
                                    NewChat(
                                        selectNewChat = selectChat,
                                        navigationIcon = navigationIcon
                                    )
                                }
                            }
                        }
                        AnimatedVisibility(
                            modifier = Modifier.onSizeChanged {
                                permanentDrawerWidthInPixels = it.width
                            },
                            visible = !notExpanded && isChatDetailsOpen,
                            enter = fadeIn() + expandHorizontally(),
                            exit = fadeOut() + shrinkHorizontally()
                        ) {
                            PermanentDrawerSheet(
                                drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation((0.2).dp),
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
