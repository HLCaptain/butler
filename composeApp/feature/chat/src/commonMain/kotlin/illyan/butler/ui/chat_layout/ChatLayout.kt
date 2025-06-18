package illyan.butler.ui.chat_layout

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.core.ui.utils.BackHandler
import illyan.butler.ui.chat_detail.ChatDetail
import illyan.butler.ui.chat_detail.ChatDetailViewModel
import illyan.butler.ui.chat_details.ChatDetails
import illyan.butler.ui.new_chat.NewChat
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

enum class ChatLayoutDestinations {
    MODEL_SELECTION,
    CHAT_SETTINGS,
}

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class, ExperimentalUuidApi::class)
@Composable
fun ChatLayout(
    modifier: Modifier = Modifier,
    currentChat: Uuid?,
    onCurrentChatChanged: (Uuid?) -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
        val viewModel = koinViewModel<ChatDetailViewModel>()
        val state by viewModel.state.collectAsState()
        LaunchedEffect(currentChat) {
            viewModel.loadChat(currentChat)
        }
        LaunchedEffect(state.chat) {
            onCurrentChatChanged(state.chat?.id)
        }
        var targetDestination by rememberSaveable(
            stateSaver = object : Saver<ChatLayoutDestinations?, Int> {
                override fun restore(value: Int) = ChatLayoutDestinations.entries[value]
                override fun SaverScope.save(value: ChatLayoutDestinations?) = value?.ordinal
            }
        ) {
            mutableStateOf(null)
        }
        var destination by remember { mutableStateOf(targetDestination ?: ChatLayoutDestinations.MODEL_SELECTION) }
        BackHandler(enabled = targetDestination != null) {
            targetDestination = null
        }
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberModalBottomSheetState(
                skipPartiallyExpanded = true
            )
        )
        LaunchedEffect(targetDestination) {
            if (targetDestination == null) {
                bottomSheetScaffoldState.bottomSheetState.hide()
            } else {
                targetDestination?.let { destination = it }
                bottomSheetScaffoldState.bottomSheetState.expand()
            }
        }
        LaunchedEffect(bottomSheetScaffoldState.bottomSheetState.currentValue) {
            if (bottomSheetScaffoldState.bottomSheetState.currentValue == SheetValue.Hidden) {
                targetDestination = null
            }
        }
        val hazeState = remember { HazeState() }
        BottomSheetScaffold(
            modifier = modifier.hazeSource(hazeState),
            scaffoldState = bottomSheetScaffoldState,
            sheetContent = {
                CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp))) {
                    when (destination) {
                        ChatLayoutDestinations.MODEL_SELECTION -> {
                            NewChat(
                                selectModel = { modelConfig ->
                                    viewModel.selectNewChatModel(modelConfig)
                                    targetDestination = null
                                },
                                hazeState = hazeState,
                                navigationIcon = {
                                    IconButton(
                                        onClick = { targetDestination = null },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            )
                        }
                        ChatLayoutDestinations.CHAT_SETTINGS -> {
                            ChatDetails(
                                chatId = currentChat,
                                hazeState = hazeState,
                                navigationIcon = {
                                    IconButton(
                                        onClick = { targetDestination = null },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            },
            sheetDragHandle = null,
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp),
            sheetContainerColor = Color.Transparent,
        ) { _ ->
            ChatDetail(
                state = state,
                sendMessage = viewModel::sendMessage,
                toggleRecord = viewModel::toggleRecording,
                sendImage = viewModel::sendImage,
                playAudio = viewModel::playAudio,
                stopAudio = viewModel::stopAudio,
                refreshChat = viewModel::refreshChat,
                sendError = viewModel::sendError,
                navigationIcon = navigationIcon,
                navigateToChatSettings = {
                    targetDestination = if (targetDestination == null) ChatLayoutDestinations.CHAT_SETTINGS else null
                },
                navigateToModelSelection = {
                    targetDestination = if (targetDestination == null) ChatLayoutDestinations.MODEL_SELECTION else null
                },
            )
        }
    }
}
