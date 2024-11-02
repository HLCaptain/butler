package illyan.butler.ui.chat_layout

import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.chat_detail.ChatDetailScreen
import illyan.butler.ui.chat_detail.ChatDetailViewModel
import illyan.butler.ui.chat_list.ChatList
import illyan.butler.ui.chat_list.ChatListViewModel
import illyan.butler.ui.components.ButlerTwoPane
import illyan.butler.ui.components.FixedOffsetHorizontalTwoPaneStrategy
import illyan.butler.ui.components.FractionHorizontalTwoPaneStrategy
import io.github.aakira.napier.Napier
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun ChatScreen(
    currentChat: String?,
    onSelectChat: (String?) -> Unit
) {
    // Make your Compose Multiplatform UI
    val (height, width) = getWindowSizeInDp()
    var windowWidth by remember { mutableStateOf(width) }
    val chatListNavController = rememberNavController()
    DisposableEffect(Unit) {
        val onDestinationChangedListener = NavController.OnDestinationChangedListener { _, destination, _ ->
            if (destination.route?.contains("chatDetail") == false) onSelectChat(null)
        }
        chatListNavController.addOnDestinationChangedListener(onDestinationChangedListener)
        onDispose { chatListNavController.removeOnDestinationChangedListener(onDestinationChangedListener) }
    }
    // React properly to list-detail transitions:
    // ListOnly -> ListDetail:
    // - if Chat is opened from List:
    //   Place Chat to detail screen.
    //   Remove Chat from list screen, transitioning to original list screen.
    //   Selecting a list item will open Chat in detail screen.
    // - if Chat is not yet opened:
    //   Place empty detail screen.
    //   Selecting a list item will open Chat in detail screen.
    // ListDetail -> ListOnly:
    // - if Chat is opened from List:
    //   Add Chat to list screen, clear detail navigator.
    //   Selecting a list item will open Chat in list screen.
    // - if Chat is not yet opened:
    //   Selecting a list item will open Chat in list screen.
    //   Clear detail navigator.
    val compactPaneStrategy = remember { FractionHorizontalTwoPaneStrategy(1f) }
    val mediumPaneStrategy = remember { FractionHorizontalTwoPaneStrategy(0.4f) }
    val largePaneStrategy = remember { FixedOffsetHorizontalTwoPaneStrategy(320.dp, true) }
    val currentPaneStrategy by remember {
        derivedStateOf {
            if (windowWidth < 600.dp) compactPaneStrategy
            else if (windowWidth < 1200.dp) mediumPaneStrategy
            else largePaneStrategy
        }
    }
    LaunchedEffect(width, height) {
        windowWidth = width
        Napier.v("Window size: $width x $height")
    }
    LaunchedEffect(currentPaneStrategy) {
        val strategy = when (currentPaneStrategy) {
            compactPaneStrategy -> "Compact"
            mediumPaneStrategy -> "Medium"
            else -> "Large"
        }
        Napier.v("Current pane strategy: $strategy")
    }
    val isListOnly by derivedStateOf { currentPaneStrategy == compactPaneStrategy }
    LaunchedEffect(currentChat, isListOnly) {
        Napier.v("currentChat: $currentChat, isListOnly: $isListOnly, listNavigator: $chatListNavController")
        Napier.v("listNavigator: ${chatListNavController.currentBackStack.value.joinToString { it.destination.route.toString() }}")
        if (currentChat != null) {
            Napier.v("selectedChat is not null")
            if (isListOnly) {
                if (chatListNavController.currentDestination?.route?.contains("chatDetail") == false) {
                    chatListNavController.navigate("chatDetail/${currentChat}")
                    Napier.v("Pushed ChatDetailScreen to listNavigator")
                }
            } else {
                if (chatListNavController.currentDestination?.route?.contains("chatDetail") == true) {
                    chatListNavController.popBackStack()
                    Napier.v("Removed chat from list screen.")
                }
            }
        } else {
            Napier.v("selectedChat is null")
            if (chatListNavController.currentDestination?.route?.contains("chatDetail") == true) {
                chatListNavController.popBackStack()
                Napier.v("Popped ChatDetailScreen from listNavigator")
            }
        }
    }
    CompositionLocalProvider(
        LocalChatSelector provides onSelectChat,
        LocalSelectedChat provides currentChat
    ) {
        ButlerTwoPane(
            strategy = currentPaneStrategy,
            first = {
                NavHost(
                    navController = chatListNavController,
                    startDestination = "chatList",
                    enterTransition = { slideInHorizontally { it } },
                    exitTransition = { slideOutHorizontally() + fadeOut() },
                    popEnterTransition = { slideInHorizontally() },
                    popExitTransition = { slideOutHorizontally { it } + fadeOut() }
                ) {
                    composable("chatList") {
                        val viewModel = koinViewModel<ChatListViewModel>()
                        val chats by viewModel.userChats.collectAsState()
                        ChatList(
                            chats = chats,
                            openChat = onSelectChat,
                            deleteChat = viewModel::deleteChat
                        )
                    }
                    composable("chatDetail/{chatId}") {
                        val chatId = it.arguments?.getString("chatId")
                        val viewModel = koinViewModel<ChatDetailViewModel>()
                        val state by viewModel.state.collectAsState()
                        ChatDetailScreen(state, viewModel, chatId, isListOnly) {
                            chatListNavController.popBackStack()
                        }
                    }
                }
            },
            second = {
                val viewModel = koinViewModel<ChatDetailViewModel>()
                val state by viewModel.state.collectAsState()
                ChatDetailScreen(state, viewModel, currentChat, false)
            }
        )
    }
}

val LocalChatSelector = compositionLocalOf<((String?) -> Unit)> { {} }
val LocalChatBackHandler = compositionLocalOf { {} }
val LocalSelectedChat = compositionLocalOf<String?> { null }
