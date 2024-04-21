package illyan.butler.ui.chat_layout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.chat_detail.ChatDetailScreen
import illyan.butler.ui.chat_list.ChatListScreen
import illyan.butler.ui.components.ButlerTwoPane
import illyan.butler.ui.components.FixedOffsetHorizontalTwoPaneStrategy
import illyan.butler.ui.components.FractionHorizontalTwoPaneStrategy
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.MutableStateFlow

class ChatScreen(selectedChat: String? = null) : Screen {
    val selectedChat = MutableStateFlow(selectedChat)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ChatScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
        val (height, width) = getWindowSizeInDp()
        var windowWidth by remember { mutableStateOf(width) }
        var currentChat by rememberSaveable { mutableStateOf<String?>(null) }
        val selected by selectedChat.collectAsState()
        LaunchedEffect(selected) {
            currentChat = selected
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
        val isListOnly by remember { derivedStateOf { currentPaneStrategy == compactPaneStrategy } }
        var listNavigator by rememberSaveable { mutableStateOf<Navigator?>(null) }
        var detailNavigator by rememberSaveable { mutableStateOf<Navigator?>(null) }
        val onChatDetailBack = {
            currentChat = null
            if (isListOnly) {
                listNavigator?.popUntil { it is ChatListScreen }
            }
        }
        val chatDetailScreen by remember { lazy { ChatDetailScreen({ currentChat }) { onChatDetailBack() } } }
        LaunchedEffect(isListOnly) {
            if (isListOnly) {
                Napier.v("Transitioning from ListDetail to ListOnly")
                if (currentChat != null) {
                    listNavigator?.push(chatDetailScreen)
                    Napier.v("Added chat onto list screen.")
                } else {
                    detailNavigator?.replaceAll(chatDetailScreen)
                    Napier.v("Placed empty detail screen.")
                }
            } else {
                Napier.v("Transitioning from ListOnly to ListDetail")
                if (currentChat != null) {
                    if (listNavigator?.lastItem !is ChatListScreen) {
                        listNavigator?.popUntil { it is ChatListScreen }
                    }
                    Napier.v("Removed chat from list screen.")
                    detailNavigator?.replaceAll(chatDetailScreen)
                    Napier.v("Added chat onto detail screen.")
                } else {
                    detailNavigator?.replaceAll(chatDetailScreen)
                    Napier.v("Placed empty detail screen.")
                }
            }
        }
        LaunchedEffect(currentChat) {
            if (currentChat != null) {
                Napier.v("selectedChat is not null")
                if (isListOnly) {
                    if (listNavigator?.lastItem !is ChatListScreen) {
                        listNavigator?.popUntil { it is ChatListScreen }
                    }
                    listNavigator?.push(chatDetailScreen)
                    Napier.v("Pushed ChatDetailScreen to listNavigator")
                } else {
                    detailNavigator?.replaceAll(chatDetailScreen)
                    Napier.v("Replaced all screens in detailNavigator with ChatDetailScreen")
                }
            } else {
                Napier.v("selectedChat is null")
                if (listNavigator?.lastItem !is ChatListScreen) {
                    listNavigator?.popUntil { it is ChatListScreen }
                }
                detailNavigator?.replaceAll(chatDetailScreen)
                Napier.v("Popped screens from listNavigator until ChatListScreen is found")
            }
        }
        ButlerTwoPane(
            strategy = currentPaneStrategy,
            first = {
                Navigator(ChatListScreen { currentChat = it; Napier.v { "Selected chat ID: $it" } }) {
                    LaunchedEffect(Unit) { listNavigator = it }
                    CurrentScreen()
                }
            },
            second = {
                Navigator(chatDetailScreen) {
                    LaunchedEffect(Unit) { detailNavigator = it }
                    CurrentScreen()
                }
            }
        )
    }
}
