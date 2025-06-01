package illyan.butler.ui.chat_layout

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.ui.chat_detail.ChatDetail
import illyan.butler.ui.chat_detail.ChatDetailViewModel
import illyan.butler.ui.chat_details.ChatDetails
import illyan.butler.ui.new_chat.NewChat
import org.koin.compose.viewmodel.koinViewModel

enum class ChatLayoutDestinations {
    MODEL_SELECTION,
    CHAT_SETTINGS,
}

@OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatLayout(
    modifier: Modifier = Modifier,
    currentChat: String?,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
        val viewModel = koinViewModel<ChatDetailViewModel>()
        val state by viewModel.state.collectAsState()
        LaunchedEffect(currentChat) {
            currentChat?.let { viewModel.loadChat(it) }
        }
        var destination by rememberSaveable(
            stateSaver = object : Saver<ChatLayoutDestinations?, Int> {
                override fun restore(value: Int): ChatLayoutDestinations? {
                    return ChatLayoutDestinations.entries[value]
                }

                override fun SaverScope.save(value: ChatLayoutDestinations?): Int? {
                    return value?.ordinal
                }
            }
        ) {
            mutableStateOf<ChatLayoutDestinations?>(null)
        }
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                initialValue = SheetValue.Hidden,
                skipHiddenState = false
            )
        )
        LaunchedEffect(destination) {
            if (destination == null) {
                bottomSheetScaffoldState.bottomSheetState.hide()
            } else {
                bottomSheetScaffoldState.bottomSheetState.expand()
            }
        }
        LaunchedEffect(bottomSheetScaffoldState.bottomSheetState.targetValue) {
            if (bottomSheetScaffoldState.bottomSheetState.targetValue == SheetValue.PartiallyExpanded) {
                destination = null
            }
        }
        Row(modifier = modifier) {
            BottomSheetScaffold(
                scaffoldState = bottomSheetScaffoldState,
                sheetContent = {
                    when (destination) {
                        ChatLayoutDestinations.MODEL_SELECTION -> {
                            NewChat(
                                selectModel = { modelConfig ->
                                    viewModel.selectNewChatModel(modelConfig)
                                    destination = null
                                },
                            )
                        }
                        ChatLayoutDestinations.CHAT_SETTINGS -> {
                            ChatDetails(
                                chatId = currentChat
                            )
                        }
                        else -> {}
                    }
                }
            ) { _ ->
                Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation((0.2).dp)) {
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
                        navigateToChatSettings = { destination = ChatLayoutDestinations.CHAT_SETTINGS },
                        navigateToModelSelection = { destination = ChatLayoutDestinations.MODEL_SELECTION },
                    )
                }
            }
        }
    }
}
