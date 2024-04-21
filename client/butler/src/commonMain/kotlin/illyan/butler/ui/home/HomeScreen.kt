package illyan.butler.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.CurrentScreen
import cafe.adriel.voyager.navigator.Navigator
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.menu
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.profile
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.arbitrary.ArbitraryScreen
import illyan.butler.ui.auth.AuthScreen
import illyan.butler.ui.chat_layout.ChatScreen
import illyan.butler.ui.components.ButlerErrorDialogContent
import illyan.butler.ui.components.GestureType
import illyan.butler.ui.components.MenuButton
import illyan.butler.ui.components.PlainTooltipWithContent
import illyan.butler.ui.dialog.ButlerDialog
import illyan.butler.ui.new_chat.NewChatScreen
import illyan.butler.ui.onboarding.OnBoardingScreen
import illyan.butler.ui.profile.ProfileDialogScreen
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        HomeScreen()
    }

    @OptIn(ExperimentalResourceApi::class)
    @Composable
    internal fun HomeScreen() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        Surface(
            modifier = Modifier.safeContentPadding(),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(0.dp)
        ) {
            Column {
                var isProfileDialogShowing by rememberSaveable { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val isUserSignedIn by remember { derivedStateOf { state.isUserSignedIn } }
                    val isTutorialDone by remember { derivedStateOf { state.isTutorialDone } }
                    var isAuthFlowEnded by remember { mutableStateOf(isUserSignedIn) }
                    LaunchedEffect(isUserSignedIn) {
                        if (isUserSignedIn != true) isAuthFlowEnded = false
                        isProfileDialogShowing = false
                    }
                    var isDialogClosedAfterTutorial by rememberSaveable { mutableStateOf(isTutorialDone) }
                    val isDialogOpen by remember {
                        derivedStateOf { isAuthFlowEnded != true || !isTutorialDone || isProfileDialogShowing }
                    }
                    LaunchedEffect(isTutorialDone) {
                        if (!isTutorialDone) isDialogClosedAfterTutorial = false
                        if (isUserSignedIn == true) isAuthFlowEnded = true
                    }
                    val startScreen by remember {
                        val onBoardingScreen by lazy { OnBoardingScreen() }
                        val profileDialogScreen by lazy { ProfileDialogScreen() }
                        val authScreen by lazy { AuthScreen() }
                        derivedStateOf {
                            if (!isDialogOpen) {
                                null
                            } else {
                                if (isTutorialDone && isDialogClosedAfterTutorial) {
                                    if (isAuthFlowEnded == true && isUserSignedIn == true && isProfileDialogShowing) profileDialogScreen else authScreen
                                } else onBoardingScreen
                            }
                        }
                    }

                    ButlerDialog(
                        startScreens = listOfNotNull(startScreen),
                        isDialogOpen = isDialogOpen,
                        isDialogFullscreen = isUserSignedIn != true || !isTutorialDone,
                        onDismissDialog = {
                            if (isUserSignedIn == true) {
                                isAuthFlowEnded = true
                            }
                            isProfileDialogShowing = false
                        },
                        onDialogClosed = {
                            if (isTutorialDone) {
                                isDialogClosedAfterTutorial = true
                            }
                        }
                    )

                    val numberOfErrors by remember { derivedStateOf { state.appErrors.size + state.serverErrors.size } }
                    val errorScreen by remember {
                        derivedStateOf {
                            ArbitraryScreen {
                                val serverErrorContent = @Composable {
                                    state.serverErrors.maxByOrNull { it.second.timestamp }?.let {
                                        ButlerErrorDialogContent(
                                            errorResponse = it.second,
                                            onClose = { screenModel.clearError(it.first) }
                                        )
                                    }
                                }
                                val appErrorContent = @Composable {
                                    state.appErrors.maxByOrNull { it.timestamp }?.let {
                                        ButlerErrorDialogContent(
                                            errorEvent = it,
                                            onClose = { screenModel.clearError(it.id) }
                                        )
                                    }
                                }
                                Crossfade(
                                    modifier = Modifier.animateContentSize(spring()),
                                    targetState = state.appErrors + state.serverErrors
                                ) { _ ->
                                    val latestAppError = state.appErrors.maxByOrNull { it.timestamp }
                                    val latestServerError = state.serverErrors.maxByOrNull { it.second.timestamp }
                                    if (latestAppError != null && latestServerError != null) {
                                        if (latestAppError.timestamp > latestServerError.second.timestamp) {
                                            appErrorContent()
                                        } else {
                                            serverErrorContent()
                                        }
                                    } else if (latestAppError != null) {
                                        appErrorContent()
                                    } else if (latestServerError != null) {
                                        serverErrorContent()
                                    }
                                }
                            }
                        }
                    }
                    ButlerDialog(
                        modifier = Modifier.zIndex(1f),
                        startScreens = listOf(errorScreen),
                        isDialogOpen = numberOfErrors > 0,
                        isDialogFullscreen = false,
                        onDismissDialog = screenModel::removeLastError
                    )
                }

                var navigator by remember { mutableStateOf<Navigator?>(null) }
                val chatScreen by remember { lazy { ChatScreen() } }
                val newChatScreen by remember {
                    lazy {
                        NewChatScreen { newChat ->
                            navigator?.replaceAll(chatScreen)
                            chatScreen.selectedChat.update { newChat }
                        }
                    }
                }
                val (height, width) = getWindowSizeInDp()
                var windowWidth by remember { mutableStateOf(width) }
                val navBarOrientation by remember {
                    derivedStateOf {
                        if (windowWidth < 600.dp) Orientation.Horizontal
                        else if (windowWidth < 1200.dp) Orientation.Vertical
                        else Orientation.Vertical
                    }
                }
                val isNavBarCompact by remember { derivedStateOf { windowWidth < 1200.dp } }
                LaunchedEffect(width, height) { windowWidth = width }
                val homeContent = @Composable {
                    Box(
                        modifier = Modifier.weight(1f, fill = true)
                    ) {
                        Navigator(chatScreen) {
                            LaunchedEffect(Unit) {
                                navigator = it
                            }
                            CurrentScreen()
                        }
                    }
                }
                val coroutineScope = rememberCoroutineScope()
                val homeContentWithVerticalNavBar = @Composable {
                    Crossfade(isNavBarCompact) { isCompact ->
                        if (isCompact) {
                            val drawerState = rememberDrawerState(DrawerValue.Closed)
                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                drawerContent = {
                                    NavigationDrawerContent(
                                        currentScreen = navigator?.lastItem,
                                        isProfileShown = isProfileDialogShowing,
                                        onProfileClick = {
                                            isProfileDialogShowing = true
                                            coroutineScope.launch { drawerState.close() }
                                        },
                                        closeDrawer = { coroutineScope.launch { drawerState.close() } },
                                        navigateToChats = {
                                            navigator?.replaceAll(chatScreen)
                                            coroutineScope.launch { drawerState.close() }
                                        },
                                        navigateToNewChat = {
                                            navigator?.replaceAll(newChatScreen)
                                            coroutineScope.launch { drawerState.close() }
                                        }
                                    )
                                }
                            ) {
                                Row {
                                    NavigationRail(
                                        header = {
                                            HamburgerButton { coroutineScope.launch { drawerState.open() } }
                                            NewChatFAB { navigator?.replaceAll(newChatScreen) }
                                        }
                                    ) {
                                        ChatsNavigationRailItem(selected = navigator?.lastItem == chatScreen && !isProfileDialogShowing) {
                                            navigator?.replaceAll(chatScreen)
                                        }
                                        NewChatNavigationRailItem(selected = navigator?.lastItem == newChatScreen && !isProfileDialogShowing) {
                                            navigator?.replaceAll(newChatScreen)
                                        }
                                        ProfileNavigationRailItem(selected = isProfileDialogShowing) {
                                            isProfileDialogShowing = true
                                        }
                                    }
                                    homeContent()
                                }
                            }
                        } else {
                            PermanentNavigationDrawer(
                                drawerContent = {
                                    NavigationDrawerContent(
                                        currentScreen = navigator?.lastItem,
                                        isProfileShown = isProfileDialogShowing,
                                        onProfileClick = { isProfileDialogShowing = true },
                                        navigateToChats = { navigator?.replaceAll(chatScreen) },
                                        navigateToNewChat = { navigator?.replaceAll(newChatScreen) },
                                        isDrawerPermanent = true
                                    )
                                }
                            ) {
                                homeContent()
                            }
                        }
                    }
                }
                val horizontalNavBar = @Composable {
                    NavigationBar {
                        ChatsNavigationBarItem(selected = navigator?.lastItem == chatScreen && !isProfileDialogShowing) {
                            navigator?.replaceAll(chatScreen)
                        }
                        NewChatNavigationBarItem(selected = navigator?.lastItem == newChatScreen && !isProfileDialogShowing) {
                            navigator?.replaceAll(newChatScreen)
                        }
                        ProfileNavigationBarItem(selected = isProfileDialogShowing) {
                            isProfileDialogShowing = true
                        }
                    }
                }
                Crossfade(navBarOrientation) { orientation ->
                    when (orientation) {
                        Orientation.Vertical -> homeContentWithVerticalNavBar()
                        Orientation.Horizontal -> Column {
                            homeContent()
                            horizontalNavBar()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HorizontalNavBar(
    isCompact: Boolean = false,
    onProfileClick: () -> Unit = {},
    onNewChatClick: () -> Unit = {},
    onChatsClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Crossfade(isCompact) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                if (it) {
                    CompactChatsButton(onClick = onChatsClick)
                    CompactNewChatButton(onClick = onNewChatClick)
                    CompactProfileButton(onClick = onProfileClick)
                } else {
                    ChatsButton(onClick = onChatsClick)
                    NewChatButton(onClick = onNewChatClick)
                    ProfileButton(onClick = onProfileClick)
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun CompactChatsButton(onClick: () -> Unit = {}) {
    PlainTooltipWithContent(
        tooltip = { Text(stringResource(Res.string.chats)) },
        enabledGestures = getNavBarTooltipGestures(),
        content = { modifier ->
            IconButton(
                onClick = onClick
            ) {
                Icon(
                    modifier = modifier,
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = stringResource(Res.string.chats)
                )
            }
        },
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun CompactProfileButton(onClick: () -> Unit = {}) {
    PlainTooltipWithContent(
        tooltip = { Text(stringResource(Res.string.profile)) },
        enabledGestures = getNavBarTooltipGestures(),
        content = { modifier ->
            IconButton(
                onClick = onClick
            ) {
                Icon(
                    modifier = modifier,
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(Res.string.profile)
                )
            }
        }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun CompactNewChatButton(onClick: () -> Unit = {}) {
    PlainTooltipWithContent(
        tooltip = { Text(stringResource(Res.string.new_chat)) },
        enabledGestures = getNavBarTooltipGestures(),
        content = { modifier ->
            IconButton(
                onClick = onClick
            ) {
                Icon(
                    modifier = modifier,
                    imageVector = Icons.Filled.Add,
                    contentDescription = stringResource(Res.string.new_chat)
                )
            }
        }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ChatsButton(onClick: () -> Unit = {}) {
    MenuButton(
        text = stringResource(Res.string.chats),
        onClick = onClick
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun NewChatButton(onClick: () -> Unit = {}) {
    MenuButton(
        text = stringResource(Res.string.new_chat),
        onClick = onClick
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ProfileButton(onClick: () -> Unit = {}) {
    Button(onClick = onClick) {
        Text(stringResource(Res.string.profile))
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ChatsNavigationRailItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = stringResource(Res.string.chats)
            )
        },
        label = { Text(stringResource(Res.string.chats)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun NewChatNavigationRailItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(Res.string.new_chat)
            )
        },
        label = { Text(stringResource(Res.string.new_chat)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun ProfileNavigationRailItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(Res.string.profile)
            )
        },
        label = { Text(stringResource(Res.string.profile)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatsNavigationDrawerItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationDrawerItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = stringResource(Res.string.chats)
            )
        },
        label = { Text(stringResource(Res.string.chats)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun NewChatNavigationDrawerItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationDrawerItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(Res.string.new_chat)
            )
        },
        label = { Text(stringResource(Res.string.new_chat)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ProfileNavigationDrawerItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationDrawerItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(Res.string.profile)
            )
        },
        label = { Text(stringResource(Res.string.profile)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun NewChatFAB(onClick: () -> Unit = {}) {
    FloatingActionButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Filled.Create,
            contentDescription = stringResource(Res.string.new_chat)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun HamburgerButton(onClick: () -> Unit = {}) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = stringResource(Res.string.menu)
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun CloseButton(onClick: () -> Unit = {}) {
    IconButton(
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(Res.string.close)
        )
    }
}

@Composable
private fun NavigationDrawerContent(
    currentScreen: Screen? = null,
    isProfileShown: Boolean = false,
    onProfileClick: () -> Unit = {},
    closeDrawer: () -> Unit = {},
    navigateToChats: () -> Unit = {},
    navigateToNewChat: () -> Unit = {},
    isDrawerPermanent: Boolean = false
) {
    ModalDrawerSheet {
        AnimatedVisibility(!isDrawerPermanent) {
            CloseButton {
                closeDrawer()
            }
        }
        ChatsNavigationDrawerItem(selected = currentScreen is ChatScreen && !isProfileShown) {
            navigateToChats()
        }
        NewChatNavigationDrawerItem(selected = currentScreen is NewChatScreen && !isProfileShown) {
            navigateToNewChat()
        }
        ProfileNavigationDrawerItem(selected = isProfileShown) {
            onProfileClick()
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RowScope.ChatsNavigationBarItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Chat,
                contentDescription = stringResource(Res.string.chats)
            )
        },
        label = { Text(stringResource(Res.string.chats)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RowScope.NewChatNavigationBarItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(Res.string.new_chat)
            )
        },
        label = { Text(stringResource(Res.string.new_chat)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun RowScope.ProfileNavigationBarItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(Res.string.profile)
            )
        },
        label = { Text(stringResource(Res.string.profile)) }
    )
}

@Composable
fun VerticalNavBar(
    isCompact: Boolean = false,
    onProfileClick: () -> Unit = {},
    onNewChatClick: () -> Unit = {},
    onChatsClick: () -> Unit = {}
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
    ) {
        Crossfade(isCompact) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    if (it) {
                        CompactChatsButton(onClick = onChatsClick)
                        CompactNewChatButton(onClick = onNewChatClick)
                    } else {
                        ChatsButton(onClick = onChatsClick)
                        NewChatButton(onClick = onNewChatClick)

                    }
                }
                if (it) {
                    CompactProfileButton(onClick = onProfileClick)
                } else {
                    ProfileButton(onClick = onProfileClick)
                }
            }
        }
    }
}

expect fun getNavBarTooltipGestures(): List<GestureType>
