package illyan.butler.ui.chat_layout

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.window.core.layout.WindowWidthSizeClass
import illyan.butler.core.ui.utils.BackHandler
import illyan.butler.ui.chat_detail.ChatDetailScreen
import illyan.butler.ui.chat_detail.ChatDetailViewModel
import illyan.butler.ui.chat_list.ChatList
import illyan.butler.ui.chat_list.ChatListViewModel
import io.github.aakira.napier.Napier
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun ChatScreen(currentChat: String?) {
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
        Napier.d("ChatScreen: currentChat=$currentChat")
        currentChat?.let { chatId = it }
    }
    LaunchedEffect(chatId) {
        chatId?.let { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it) }
    }
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
                ChatDetailScreen(state, viewModel, navigator.currentDestination?.content, navigator.canNavigateBack()) {
                    chatId = null
                    navigator.navigateBack()
                }
            }
        }
    )
}
