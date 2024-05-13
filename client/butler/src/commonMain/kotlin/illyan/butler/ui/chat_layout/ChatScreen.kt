package illyan.butler.ui.chat_layout

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import illyan.butler.domain.model.DomainChat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_chats
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.chat_detail.ChatDetailScreen
import illyan.butler.ui.chat_list.ChatListScreen
import illyan.butler.ui.components.ButlerTwoPane
import illyan.butler.ui.components.FixedOffsetHorizontalTwoPaneStrategy
import illyan.butler.ui.components.FractionHorizontalTwoPaneStrategy
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class ChatScreen : Screen {
    @Composable
    override fun Content() {
        val selectedChat = LocalSelectedChat.current
        val screenModel = koinScreenModel<ChatScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
        val (height, width) = getWindowSizeInDp()
        var windowWidth by remember { mutableStateOf(width) }
        var currentChat by rememberSaveable { mutableStateOf<String?>(null) }
        LaunchedEffect(selectedChat) {
            currentChat = selectedChat
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
        var listNavigator = remember<Navigator?> { null }
        var detailNavigator = remember<Navigator?> { null }
        val onChatDetailBack = {
            currentChat = null
            if (isListOnly) {
                listNavigator?.replaceAll(listNavigator?.items?.first()!!)
            }
        }
        val chatDetailScreen = ChatDetailScreen(currentChat)
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
                    if (listNavigator?.lastItem is ChatDetailScreen) {
                        listNavigator?.replaceAll(listNavigator?.items?.first()!!)
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
                    if (listNavigator?.lastItem is ChatDetailScreen) {
                        listNavigator?.replaceAll(listNavigator?.items?.first()!!)
                    }
                    listNavigator?.push(chatDetailScreen)
                    Napier.v("Pushed ChatDetailScreen to listNavigator")
                } else {
                    detailNavigator?.replaceAll(chatDetailScreen)
                    Napier.v("Replaced all screens in detailNavigator with ChatDetailScreen")
                }
            } else {
                Napier.v("selectedChat is null")
                if (listNavigator?.lastItem is ChatDetailScreen) {
                    listNavigator?.replaceAll(listNavigator?.items?.first()!!)
                }
                detailNavigator?.replaceAll(chatDetailScreen)
                Napier.v("Popped screens from listNavigator until ChatListScreen is found")
            }
        }
        ButlerTwoPane(
            strategy = currentPaneStrategy,
            first = {
                CompositionLocalProvider(LocalChatSelector provides { currentChat = it }) {
                    Navigator(ChatListScreen()) {
                        LaunchedEffect(Unit) { listNavigator = it }
                        SlideTransition(it)
                    }
                }
            },
            second = {
                CompositionLocalProvider(LocalChatBackHandler provides onChatDetailBack) {
                    Navigator(chatDetailScreen) {
                        LaunchedEffect(Unit) { detailNavigator = it }
                        SlideTransition(it)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatList(
    chats: List<DomainChat>,
    openChat: (uuid: String) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.chats),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { insetsPadding ->
        Crossfade(
            modifier = Modifier.padding(insetsPadding),
            targetState = chats.isEmpty()
        ) {
            if (it) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(Res.string.no_chats),
                    style = MaterialTheme.typography.headlineLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.animateContentSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(chats) { chat ->
                        ChatCard(
                            chat = chat,
                            openChat = { openChat(chat.id!!) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatCard(
    chat: DomainChat,
    openChat: () -> Unit
) {
    ElevatedCard(
        onClick = openChat
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = chat.name ?: stringResource(Res.string.new_chat),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = chat.id!!.take(16),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

val LocalChatSelector = compositionLocalOf<((String) -> Unit)> { {} }
val LocalChatBackHandler = compositionLocalOf { {} }
val LocalSelectedChat = compositionLocalOf<String?> { null }
