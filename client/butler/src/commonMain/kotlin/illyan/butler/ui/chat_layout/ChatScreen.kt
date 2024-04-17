package illyan.butler.ui.chat_layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.arbitrary.ArbitraryScreen
import illyan.butler.ui.chat_detail.ChatDetailScreen
import illyan.butler.ui.chat_list.ChatListScreen
import illyan.butler.ui.components.ButlerListDetail
import illyan.butler.ui.components.FixedOffsetHorizontalTwoPaneStrategy
import illyan.butler.ui.components.FractionHorizontalTwoPaneStrategy
import io.github.aakira.napier.Napier

class ChatScreen : Screen {
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<ChatScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
        val (height, width) = getWindowSizeInDp()
        var selectedChat by rememberSaveable { mutableStateOf<String?>(null) }
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
        val compactPaneStrategy = FractionHorizontalTwoPaneStrategy(1f)
        val mediumPaneStrategy = FractionHorizontalTwoPaneStrategy(0.4f)
        val largePaneStrategy = FixedOffsetHorizontalTwoPaneStrategy(320.dp, true)
        LaunchedEffect(width) {
            Napier.v("Window size (dp): $width x $height")
        }
        val currentPaneStrategy = if (width.value < 600) compactPaneStrategy
        else if (width.value < 1200) mediumPaneStrategy
        else largePaneStrategy
        LaunchedEffect(currentPaneStrategy) {
            val strategy = when (currentPaneStrategy) {
                compactPaneStrategy -> "Compact"
                mediumPaneStrategy -> "Medium"
                else -> "Large"
            }
            Napier.v("Current pane strategy: $strategy")
        }
        val isListOnly by rememberSaveable { derivedStateOf { currentPaneStrategy == compactPaneStrategy } }
        var listNavigator by rememberSaveable { mutableStateOf<Navigator?>(null) }
        var detailNavigator by rememberSaveable { mutableStateOf<Navigator?>(null) }
        val chatDetailScreen by remember { derivedStateOf { ChatDetailScreen { selectedChat } } }
        ButlerListDetail(
            strategy = currentPaneStrategy,
            list = {
                Navigator(ChatListScreen { selectedChat = it; Napier.v { "Selected chat ID: $it" } }) {
                    LaunchedEffect(Unit) { listNavigator = it }
                    CurrentScreen()
                }
            },
            detail = {
                Navigator(ArbitraryScreen { EmptyChatScreen() }) {
                    LaunchedEffect(Unit) { detailNavigator = it }
                    CurrentScreen()
                }
            }
        )
        LaunchedEffect(isListOnly) {
            if (isListOnly) {
                Napier.v("Transitioning from ListDetail to ListOnly")
                if (selectedChat != null) {
                    listNavigator?.push(chatDetailScreen)
                    Napier.v("Added chat onto list screen.")
                } else {
                    detailNavigator?.replaceAll(ArbitraryScreen { EmptyChatScreen() })
                    Napier.v("Placed empty detail screen.")
                }
            } else {
                Napier.v("Transitioning from ListOnly to ListDetail")
                if (selectedChat != null) {
                    if (listNavigator?.lastItem !is ChatListScreen) {
                        listNavigator?.popUntil { it is ChatListScreen }
                    }
                    Napier.v("Removed chat from list screen.")
                    detailNavigator?.replaceAll(chatDetailScreen)
                    Napier.v("Added chat onto detail screen.")
                } else {
                    detailNavigator?.replaceAll(ArbitraryScreen { EmptyChatScreen() })
                    Napier.v("Placed empty detail screen.")
                }
            }
        }
        LaunchedEffect(selectedChat) {
            if (selectedChat != null) {
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
                detailNavigator?.replaceAll(ArbitraryScreen { EmptyChatScreen() })
                Napier.v("Popped screens from listNavigator until ChatListScreen is found")
            }
        }
    }
}

@Composable
fun EmptyChatScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Select a chat")
    }
}