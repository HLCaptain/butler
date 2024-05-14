package illyan.butler.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DrawerDefaults
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
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.CrossfadeTransition
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.menu
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.profile
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.auth.AuthScreen
import illyan.butler.ui.auth_success.AuthSuccessScreen
import illyan.butler.ui.auth_success.LocalAuthSuccessDone
import illyan.butler.ui.chat_layout.ChatScreen
import illyan.butler.ui.chat_layout.LocalChatSelector
import illyan.butler.ui.chat_layout.LocalSelectedChat
import illyan.butler.ui.components.GestureType
import illyan.butler.ui.components.PlainTooltipWithContent
import illyan.butler.ui.dialog.ButlerDialog
import illyan.butler.ui.error.ErrorScreen
import illyan.butler.ui.new_chat.NewChatScreen
import illyan.butler.ui.permission.PermissionRequestScreen
import illyan.butler.ui.profile.ProfileDialogScreen
import illyan.butler.ui.select_host_tutorial.LocalSelectHostCallback
import illyan.butler.ui.select_host_tutorial.SelectHostTutorialScreen
import illyan.butler.ui.signup_tutorial.LocalSignInCallback
import illyan.butler.ui.signup_tutorial.SignUpTutorialScreen
import illyan.butler.ui.usage_tutorial.LocalUsageTutorialDone
import illyan.butler.ui.usage_tutorial.UsageTutorialScreen
import illyan.butler.ui.welcome.LocalWelcomeScreenDone
import illyan.butler.ui.welcome.WelcomeScreen
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

class HomeScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<HomeScreenModel>()
        val state by screenModel.state.collectAsState()
        Surface(
            modifier = Modifier.safeContentPadding(),
            color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
        ) {
            Column {
                var isProfileDialogShowing by rememberSaveable { mutableStateOf(false) }
                var isAuthFlowEnded by remember { mutableStateOf(state.isUserSignedIn) }
                LaunchedEffect(state.isUserSignedIn) {
                    if (isAuthFlowEnded == null) isAuthFlowEnded = state.isUserSignedIn
                    if (state.isUserSignedIn != true) isAuthFlowEnded = false
                    isProfileDialogShowing = false
                }
                var isDialogOpen by rememberSaveable { mutableStateOf(isAuthFlowEnded != true || state.isTutorialDone == false || isProfileDialogShowing) }
                LaunchedEffect(isAuthFlowEnded, state.isTutorialDone, isProfileDialogShowing) {
                    isDialogOpen = isAuthFlowEnded != true || state.isTutorialDone == false || isProfileDialogShowing
                }
                var isDialogClosedAfterTutorial by rememberSaveable { mutableStateOf(state.isTutorialDone) }
                LaunchedEffect(state.isTutorialDone) {
                    if (isDialogClosedAfterTutorial == null) isDialogClosedAfterTutorial = state.isTutorialDone
                    if (state.isTutorialDone == false) isDialogClosedAfterTutorial = false
                    if (state.isUserSignedIn == true || state.isTutorialDone == true) isAuthFlowEnded = true
                }
                val usageTutorialScreen by remember { lazy { UsageTutorialScreen() } }
                val authSuccessScreen by remember { lazy { AuthSuccessScreen(1000) } }
                val signUpTutorialScreen by remember { lazy { SignUpTutorialScreen() } }
                val selectHostTutorialScreen by remember { lazy { SelectHostTutorialScreen() } }
                val welcomeScreen by remember { lazy { WelcomeScreen() } }
                val profileDialogScreen = remember { ProfileDialogScreen() }
                val authScreen = remember { AuthScreen() }
                val startScreen by remember {
                    derivedStateOf {
                        if (!isDialogOpen) {
                            null
                        } else {
                            if (state.isTutorialDone == true && isDialogClosedAfterTutorial == true) {
                                if (isAuthFlowEnded == true && state.isUserSignedIn == true && isProfileDialogShowing) profileDialogScreen else authScreen
                            } else welcomeScreen
                        }
                    }
                }
                var onBoardingNavigator by remember { mutableStateOf<Navigator?>(null) }
                CompositionLocalProvider(
                    LocalWelcomeScreenDone provides { onBoardingNavigator?.push(selectHostTutorialScreen) },
                    LocalSelectHostCallback provides { onBoardingNavigator?.push(signUpTutorialScreen) },
                    LocalSignInCallback provides { onBoardingNavigator?.replaceAll(authSuccessScreen) },
                    LocalAuthSuccessDone provides { onBoardingNavigator?.replaceAll(usageTutorialScreen) },
                    LocalUsageTutorialDone provides { screenModel.setTutorialDone() },
                ) {
                    ButlerDialog(
                        startScreens = listOfNotNull(startScreen),
                        isDialogOpen = isDialogOpen,
                        isDialogFullscreen = state.isUserSignedIn != true || state.isTutorialDone == false,
                        onDismissDialog = {
                            if (state.isUserSignedIn == true) {
                                isAuthFlowEnded = true
                            }
                            isProfileDialogShowing = false
                        },
                        onDialogClosed = {
                            if (state.isTutorialDone == true) {
                                isDialogClosedAfterTutorial = true
                            }
                        },
                        onNavigatorSet = { onBoardingNavigator = it }
                    )
                }

                val numberOfErrors = state.appErrors.size + state.serverErrors.size
                val errorScreen = remember { ErrorScreen() }
                ButlerDialog(
                    modifier = Modifier.zIndex(1f),
                    startScreens = listOf(errorScreen),
                    isDialogOpen = numberOfErrors > 0,
                    isDialogFullscreen = false,
                    onDismissDialog = screenModel::removeLastError
                )
                val permissionRequestScreen by remember { lazy { PermissionRequestScreen() } }
                ButlerDialog(
                    modifier = Modifier.zIndex(2f),
                    startScreens = listOf(permissionRequestScreen),
                    isDialogOpen = state.preparedPermissionsToRequest.isNotEmpty(),
                    isDialogFullscreen = false,
                    onDismissDialog = screenModel::removeLastPermissionRequest
                )
                var currentScreenIndex by rememberSaveable { mutableStateOf(0) }
                var navigator by remember { mutableStateOf<Navigator?>(null) }
                val chatScreen by remember { lazy { ChatScreen() } }
                val newChatScreen by remember { lazy { NewChatScreen() } }
                val screens by remember { mutableStateOf(listOf(chatScreen, newChatScreen)) }
                // Index is rememberSaveable, Screen is probably not.
                val currentScreen = screens[currentScreenIndex]
                LaunchedEffect(currentScreen) { navigator?.replaceAll(currentScreen) }
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
                val coroutineScope = rememberCoroutineScope()
                var selectedChat by rememberSaveable { mutableStateOf<String?>(null) }
                val selectChat = { chat: String? ->
                    Napier.d("Selected chat: $chat")
                    selectedChat = chat
                    currentScreenIndex = 0
                }
                CompositionLocalProvider(
                    LocalSelectedChat provides selectedChat,
                    LocalChatSelector provides selectChat
                ) {
                    Crossfade(navBarOrientation) { orientation ->
                        when (orientation) {
                            Orientation.Vertical -> HomeContentWithVerticalNavBar(
                                isNavBarCompact,
                                currentScreen,
                                isProfileDialogShowing,
                                coroutineScope,
                                currentScreenIndex,
                                screens,
                                chatScreen,
                                newChatScreen,
                                navBarOrientation,
                                setCurrentDialogShowing = { isProfileDialogShowing = it },
                                setCurrentScreenIndex = { currentScreenIndex = it },
                                setNavigator = { navigator = it }
                            )

                            Orientation.Horizontal -> Column {
                                Row(Modifier.weight(1f)) {
                                    HomeContent(navBarOrientation, currentScreen) { navigator = it }
                                }
                                HorizontalNavBar(
                                    currentScreen,
                                    chatScreen,
                                    isProfileDialogShowing,
                                    setProfileDialogShowing = { isProfileDialogShowing = it },
                                    setCurrentScreenIndex = { currentScreenIndex = it },
                                    screens,
                                    newChatScreen
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HorizontalNavBar(
        currentScreen: Screen,
        chatScreen: ChatScreen,
        isProfileDialogShowing: Boolean,
        setProfileDialogShowing: (Boolean) -> Unit,
        setCurrentScreenIndex: (Int) -> Unit,
        screens: List<Screen>,
        newChatScreen: NewChatScreen
    ) {
        NavigationBar {
            ChatsNavigationBarItem(selected = currentScreen == chatScreen && !isProfileDialogShowing) {
                setCurrentScreenIndex(screens.indexOf(chatScreen))
            }
            NewChatNavigationBarItem(selected = currentScreen == newChatScreen && !isProfileDialogShowing) {
                setCurrentScreenIndex(screens.indexOf(newChatScreen))
            }
            ProfileNavigationBarItem(selected = isProfileDialogShowing) {
                setProfileDialogShowing(true)
            }
        }
    }

    @Composable
    private fun HomeContentWithVerticalNavBar(
        isNavBarCompact: Boolean,
        currentScreen: Screen,
        isProfileDialogShowing: Boolean,
        coroutineScope: CoroutineScope,
        currentScreenIndex: Int,
        screens: List<Screen>,
        chatScreen: ChatScreen,
        newChatScreen: NewChatScreen,
        navBarOrientation: Orientation,
        setCurrentDialogShowing: (Boolean) -> Unit,
        setCurrentScreenIndex: (Int) -> Unit,
        setNavigator: (Navigator) -> Unit
    ) {
        Crossfade(isNavBarCompact) { isCompact ->
            if (isCompact) {
                val drawerState = rememberDrawerState(DrawerValue.Closed)
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        NavigationDrawerContent(
                            currentScreen = currentScreen,
                            isProfileShown = isProfileDialogShowing,
                            onProfileClick = {
                                setCurrentDialogShowing(true)
                                coroutineScope.launch { drawerState.close() }
                            },
                            closeDrawer = { coroutineScope.launch { drawerState.close() } },
                            navigateToChats = {
                                setCurrentScreenIndex(screens.indexOf(chatScreen))
                                coroutineScope.launch { drawerState.close() }
                            },
                            navigateToNewChat = {
                                setCurrentScreenIndex(screens.indexOf(newChatScreen))
                                coroutineScope.launch { drawerState.close() }
                            }
                        )
                    }
                ) {
                    Row {
                        NavigationRail(
                            containerColor = Color.Transparent,
                            header = {
                                HamburgerButton { coroutineScope.launch { drawerState.open() } }
                                NewChatFAB { setCurrentScreenIndex(screens.indexOf(newChatScreen)) }
                            }
                        ) {
                            Spacer(Modifier.weight(0.5f))
                            ChatsNavigationRailItem(selected = screens[currentScreenIndex] == chatScreen && !isProfileDialogShowing) {
                                setCurrentScreenIndex(screens.indexOf(chatScreen))
                            }
                            NewChatNavigationRailItem(selected = screens[currentScreenIndex] == newChatScreen && !isProfileDialogShowing) {
                                setCurrentScreenIndex(screens.indexOf(newChatScreen))
                            }
                            ProfileNavigationRailItem(selected = isProfileDialogShowing) {
                                setCurrentDialogShowing(true)
                            }
                            Spacer(Modifier.weight(1f))
                        }
                        HomeContent(navBarOrientation, currentScreen, setNavigator)
                    }
                }
            } else {
                PermanentNavigationDrawer(
                    drawerContent = {
                        NavigationDrawerContent(
                            currentScreen = currentScreen,
                            isProfileShown = isProfileDialogShowing,
                            onProfileClick = { setCurrentDialogShowing(true) },
                            navigateToChats = { setCurrentScreenIndex(screens.indexOf(chatScreen)) },
                            navigateToNewChat = { setCurrentScreenIndex(screens.indexOf(newChatScreen)) },
                            isDrawerPermanent = true
                        )
                    }
                ) {
                    HomeContent(navBarOrientation, currentScreen, setNavigator)
                }
            }
        }
    }

    @Composable
    private fun HomeContent(
        navBarOrientation: Orientation,
        currentScreen: Screen,
        setNavigator: (Navigator) -> Unit
    ) {
        Surface(
            tonalElevation = 0.dp,
            shape = RoundedCornerShape(topStart = if (navBarOrientation == Orientation.Vertical) 24.dp else 0.dp)
        ) {
            Navigator(currentScreen) {
                LaunchedEffect(Unit) { setNavigator(it) }
                CrossfadeTransition(navigator = it)
            }
        }
    }

    @Composable
    private fun OnboardingContent(
        navigator: Navigator?,
        setTutorialDone: () -> Unit
    ) {
        // (language selection may be in a corner?)
        // 1. Show welcome screen
        // 2. Show host selection tutorial
        // 3. Show sign up tutorial
        // 4. Show usage tutorial
        // 5. If done, set tutorial done.

        val usageTutorialScreen by remember { lazy { UsageTutorialScreen() } }
        val authSuccessScreen by remember { lazy { AuthSuccessScreen(1000) } }
        val signUpTutorialScreen by remember { lazy { SignUpTutorialScreen() } }
        val selectHostTutorialScreen by remember { lazy { SelectHostTutorialScreen() } }
        val welcomeScreen by remember { lazy { WelcomeScreen() } }

        CompositionLocalProvider(
            LocalWelcomeScreenDone provides { navigator?.push(selectHostTutorialScreen) },
            LocalSelectHostCallback provides { navigator?.push(signUpTutorialScreen) },
            LocalSignInCallback provides { navigator?.replaceAll(authSuccessScreen) },
            LocalAuthSuccessDone provides { navigator?.replaceAll(usageTutorialScreen) },
            LocalUsageTutorialDone provides { setTutorialDone() },
        ) {

        }
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
        icon = Icons.AutoMirrored.Filled.Chat,
        stringResource = Res.string.chats
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
        icon = Icons.Filled.Add,
        stringResource = Res.string.new_chat
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
        icon = Icons.Filled.Person,
        stringResource = Res.string.profile
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun NavigationRailItem(
    selected: Boolean = false,
    onClick: () -> Unit = {},
    icon: ImageVector,
    stringResource: StringResource
) {
    NavigationRailItem(
        onClick = onClick,
        selected = selected,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(stringResource)
            )
        },
        label = { Text(stringResource(stringResource)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ChatsNavigationDrawerItem(
    selected: Boolean = false,
    onClick: () -> Unit = {}
) {
    NavigationDrawerItem(
        onClick = onClick,
        selected = selected,
        icon = Icons.AutoMirrored.Filled.Chat,
        stringResource = Res.string.chats
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
        icon = Icons.Filled.Add,
        stringResource = Res.string.new_chat
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
        icon = Icons.Filled.Person,
        stringResource = Res.string.profile
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun NavigationDrawerItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    icon: ImageVector,
    stringResource: StringResource
) {
    NavigationDrawerItem(
        modifier = modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(stringResource)
            )
        },
        label = { Text(stringResource(stringResource)) }
    )
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun NewChatFAB(onClick: () -> Unit = {}) {
    PlainTooltipWithContent(
        enabledGestures = getNavBarTooltipGestures(),
        tooltip = { Text(stringResource(Res.string.new_chat)) },
    ) { gestureAreaModifier ->
        FloatingActionButton(
            modifier = gestureAreaModifier,
            onClick = onClick
        ) {
            Icon(
                imageVector = Icons.Filled.Create,
                contentDescription = stringResource(Res.string.new_chat)
            )
        }
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
        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
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
    ModalDrawerSheet(
        drawerContainerColor = if (isDrawerPermanent) Color.Transparent else DrawerDefaults.containerColor,
        drawerShape = DrawerDefaults.shape,
        modifier = Modifier.widthIn(max = 280.dp),
    ) {
        AnimatedVisibility(!isDrawerPermanent) {
            CloseButton { closeDrawer() }
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

expect fun getNavBarTooltipGestures(): List<GestureType>
