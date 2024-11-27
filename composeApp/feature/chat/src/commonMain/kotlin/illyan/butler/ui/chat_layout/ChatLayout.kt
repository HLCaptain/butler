package illyan.butler.ui.chat_layout

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.window.core.layout.WindowWidthSizeClass
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.core.ui.utils.BackHandler
import illyan.butler.ui.chat_detail.ChatDetail
import illyan.butler.ui.chat_detail.ChatDetailViewModel
import illyan.butler.ui.chat_list.ChatList
import illyan.butler.ui.chat_list.ChatListViewModel
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun ChatLayout(currentChat: String?) {
    var chatId by rememberSaveable { mutableStateOf(currentChat) }
    val navigator = rememberListDetailPaneScaffoldNavigator<String>(
        scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()).copy(
            maxHorizontalPartitions = if (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) 1 else 2
        ),
    )
    BackHandler(navigator.canNavigateBack()) {
        chatId = null
        navigator.navigateBack()
    }
    LaunchedEffect(currentChat) {
        currentChat?.let { chatId = it }
    }
    LaunchedEffect(chatId) {
        chatId?.let { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it) }
    }
    CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    val viewModel = koinViewModel<ChatListViewModel>()
                    val chats by viewModel.userChats.collectAsState()
                    ChatList(
                        chats = chats,
                        openChat = { chatId = it },
                        deleteChat = viewModel::deleteChat
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val viewModel = koinViewModel<ChatDetailViewModel>()
                    val state by viewModel.state.collectAsState()
                    LaunchedEffect(navigator.currentDestination?.content) {
                        navigator.currentDestination?.content?.let { viewModel.loadChat(it) }
                    }
                    ChatDetail(
                        state = state,
                        sendMessage = viewModel::sendMessage,
                        toggleRecord = viewModel::toggleRecording,
                        sendImage = viewModel::sendImage,
                        playAudio = viewModel::playAudio,
                        stopAudio = viewModel::stopAudio,
                        canNavigateBack = navigator.canNavigateBack()
                    ) {
                        chatId = null
                        navigator.navigateBack()
                    }
                }
            }
        )
    }
}
