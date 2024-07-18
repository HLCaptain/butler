package illyan.butler.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.menu
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.profile
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.auth.AuthScreen
import illyan.butler.ui.auth_success.AuthSuccessIcon
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
import illyan.butler.ui.select_host.SelectHostScreen
import illyan.butler.ui.select_host_tutorial.SelectHostTutorialScreen
import illyan.butler.ui.settings.user.UserSettingsScreen
import illyan.butler.ui.signup_tutorial.SignUpTutorialScreen
import illyan.butler.ui.usage_tutorial.UsageTutorialScreen
import illyan.butler.ui.welcome.WelcomeScreen
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI

@OptIn(KoinExperimentalAPI::class)
@Composable
fun HomeScreen() {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsState()
    Surface(
//            modifier = Modifier.safeContentPadding(),
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
            val onBoardingNavController = rememberNavController()
            val profileNavController = rememberNavController()
            val authNavController = rememberNavController()
            val userFlow = remember(isDialogOpen, state.isTutorialDone, isDialogClosedAfterTutorial, isAuthFlowEnded, state.isUserSignedIn, isProfileDialogShowing) {
                if (!isDialogOpen) null
                else if (state.isTutorialDone == true && isDialogClosedAfterTutorial == true) {
                    if (isAuthFlowEnded == true && state.isUserSignedIn == true && isProfileDialogShowing) DialogUserFlow.Profile else DialogUserFlow.Auth
                } else DialogUserFlow.OnBoarding
            }

            val currentNavController = remember(userFlow) {
                Napier.d("Current user flow: $userFlow")
                when (userFlow) {
                    DialogUserFlow.Auth -> authNavController
                    DialogUserFlow.OnBoarding -> onBoardingNavController
                    DialogUserFlow.Profile -> profileNavController
                    null -> null
                }
            }

            ButlerDialog(
                isDialogOpen = isDialogOpen,
                isDialogFullscreen = state.isUserSignedIn != true || state.isTutorialDone == false,
                onDismissDialog = {
                    if (state.isUserSignedIn == true) isAuthFlowEnded = true
                    isProfileDialogShowing = false
                },
                onDialogClosed = { if (state.isTutorialDone == true) isDialogClosedAfterTutorial = true },
                navController = currentNavController
            ) {
                Crossfade(userFlow) {
                    val animationTime = 200
                    when (userFlow) {
                        DialogUserFlow.Auth -> {
                            AuthScreen(authSuccessEnded = { isAuthFlowEnded = true })
                        }

                        DialogUserFlow.OnBoarding -> {
                            NavHost(
                                navController = onBoardingNavController,
                                startDestination = "welcome",
                                enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
                                popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
                                exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
                                popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
                            ) {
                                composable("welcome") {
                                    WelcomeScreen {
                                        onBoardingNavController.navigate("selectHostTutorial")
                                    }
                                }
                                composable("selectHostTutorial") {
                                    SelectHostTutorialScreen {
                                        onBoardingNavController.navigate("selectHost")
                                    }
                                }
                                composable("selectHost") {
                                    SelectHostScreen {
                                        onBoardingNavController.navigate("signUpTutorial")
                                    }
                                }
                                composable("signUpTutorial") {
                                    SignUpTutorialScreen {
                                        onBoardingNavController.navigate("auth")
                                    }
                                }
                                composable("auth") {
                                    AuthScreen(
                                        authSuccessEnded = {
                                            // FIXME: launch single on top does not work right now
                                            //  due to bug in androidx.navigation, update dependencies
                                            onBoardingNavController.navigate("usageTutorial") { launchSingleTop = true }
                                        }
                                    )
                                }
                                composable("authSuccess") {
                                    AuthSuccessIcon()
                                    LaunchedEffect(Unit) {
                                        delay(1000L)

                                    }
                                }
                                composable("usageTutorial") {
                                    UsageTutorialScreen {
                                        viewModel.setTutorialDone()
                                        isDialogClosedAfterTutorial = true
                                    }
                                }
                            }
                        }

                        DialogUserFlow.Profile -> {
                            NavHost(
                                navController = profileNavController,
                                startDestination = "profile",
                                enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
                                popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
                                exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
                                popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
                            ) {
                                composable("profile") {
                                    ProfileDialogScreen {
                                        profileNavController.navigate("settings")
                                    }
                                }
                                composable("settings") {
                                    UserSettingsScreen()
                                }
                            }
                        }

                        null -> {}
                    }
                }
            }

            val numberOfErrors = state.appErrors.size + state.serverErrors.size
            ButlerDialog(
                modifier = Modifier.zIndex(1f),
                isDialogOpen = numberOfErrors > 0,
                isDialogFullscreen = false,
                onDismissDialog = viewModel::removeLastError
            ) { ErrorScreen() }
            ButlerDialog(
                modifier = Modifier.zIndex(2f),
                isDialogOpen = state.preparedPermissionsToRequest.isNotEmpty(),
                isDialogFullscreen = false,
                onDismissDialog = viewModel::removeLastPermissionRequest
            ) { PermissionRequestScreen() }
            // Index is rememberSaveable, Screen is probably not.
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
            val homeNavController = rememberNavController()
            val selectChat = { chat: String? ->
                Napier.d("Selected chat: $chat")
                homeNavController.navigate("chat", ) { launchSingleTop = true }
                selectedChat = chat
            }
            CompositionLocalProvider(
                LocalSelectedChat provides selectedChat,
                LocalChatSelector provides selectChat,
                LocalNavBarOrientation provides navBarOrientation
            ) {
                Crossfade(navBarOrientation) { orientation ->
                    when (orientation) {
                        Orientation.Vertical -> HomeContentWithVerticalNavBar(
                            isNavBarCompact,
                            isProfileDialogShowing,
                            coroutineScope,
                            homeNavController,
                            navBarOrientation,
                            setDialogVisibility = { isProfileDialogShowing = it },
                            navigateToChats = { homeNavController.navigate("chat") { launchSingleTop = true } },
                            navigateToNewChat = { homeNavController.navigate("newChat") { launchSingleTop = true } }
                        ) {
                            NavHost(
                                navController = homeNavController,
                                startDestination = "chat"
                            ) {
                                composable("chat") {
                                    ChatScreen(selectedChat) {
                                        selectedChat = it
                                    }
                                }
                                composable("newChat") {
                                    NewChatScreen()
                                }
                            }
                        }

                        Orientation.Horizontal -> Column {
                            Row(Modifier.weight(1f)) {
                                HomeContent(navBarOrientation) {
                                    NavHost(
                                        navController = homeNavController,
                                        startDestination = "chat"
                                    ) {
                                        composable("chat") {
                                            ChatScreen(selectedChat) {
                                                selectedChat = it
                                            }
                                        }
                                        composable("newChat") {
                                            NewChatScreen()
                                        }
                                    }
                                }
                            }
                            HorizontalNavBar(
                                homeNavController,
                                isProfileDialogShowing,
                                setProfileDialogShowing = { isProfileDialogShowing = it },
                                navigateToChats = { homeNavController.navigate("chat") { launchSingleTop = true } },
                                navigateToNewChat = { homeNavController.navigate("newChat") { launchSingleTop = true } }
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
    navController: NavController,
    isProfileDialogShowing: Boolean,
    setProfileDialogShowing: (Boolean) -> Unit,
    navigateToChats: () -> Unit,
    navigateToNewChat: () -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    NavigationBar {
        ChatsNavigationBarItem(selected = backStackEntry?.destination?.route == "chat" && !isProfileDialogShowing) {
            navigateToChats()
        }
        NewChatNavigationBarItem(selected =  backStackEntry?.destination?.route == "newChat" && !isProfileDialogShowing) {
            navigateToNewChat()
        }
        ProfileNavigationBarItem(selected = isProfileDialogShowing) {
            setProfileDialogShowing(true)
        }
    }
}

@Composable
private fun HomeContentWithVerticalNavBar(
    isNavBarCompact: Boolean,
    isProfileDialogShowing: Boolean,
    coroutineScope: CoroutineScope,
    navController: NavController,
    navBarOrientation: Orientation,
    setDialogVisibility: (Boolean) -> Unit,
    navigateToChats: () -> Unit,
    navigateToNewChat: () -> Unit,
    content: @Composable () -> Unit
) {
    Crossfade(isNavBarCompact) { isCompact ->
        if (isCompact) {
            val drawerState = rememberDrawerState(DrawerValue.Closed)
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    NavigationDrawerContent(
                        navController = navController,
                        isProfileShown = isProfileDialogShowing,
                        onProfileClick = {
                            setDialogVisibility(true)
                            coroutineScope.launch { drawerState.close() }
                        },
                        closeDrawer = { coroutineScope.launch { drawerState.close() } },
                        navigateToChats = {
                            navigateToChats()
                            coroutineScope.launch { drawerState.close() }
                        },
                        navigateToNewChat = {
                            navigateToNewChat()
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                Row {
                    NavigationRail(
                        modifier = Modifier.statusBarsPadding(),
                        containerColor = Color.Transparent,
                        header = {
                            HamburgerButton { coroutineScope.launch { drawerState.open() } }
                            NewChatFAB { navigateToNewChat() }
                        }
                    ) {
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        Spacer(Modifier.weight(0.5f))
                        ChatsNavigationRailItem(selected = backStackEntry?.destination?.route == "chat" && !isProfileDialogShowing) {
                            navigateToChats()
                        }
                        NewChatNavigationRailItem(selected = backStackEntry?.destination?.route == "newChat" && !isProfileDialogShowing) {
                            navigateToNewChat()
                        }
                        ProfileNavigationRailItem(selected = isProfileDialogShowing) {
                            setDialogVisibility(true)
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    HomeContent(navBarOrientation, content)
                }
            }
        } else {
            PermanentNavigationDrawer(
                drawerContent = {
                    NavigationDrawerContent(
                        navController = navController,
                        isProfileShown = isProfileDialogShowing,
                        onProfileClick = { setDialogVisibility(true) },
                        navigateToChats = navigateToChats,
                        navigateToNewChat = navigateToNewChat,
                        isDrawerPermanent = true
                    )
                }
            ) {
                HomeContent(navBarOrientation, content)
            }
        }
    }
}

@Composable
private fun HomeContent(
    navBarOrientation: Orientation,
    content: @Composable () -> Unit
) {
    Surface(
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = if (navBarOrientation == Orientation.Vertical) 24.dp else 0.dp)
    ) {
        content()
    }
}

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
        label = { Text(stringResource(stringResource)) },
        colors = NavigationDrawerItemDefaults.colors(
            unselectedContainerColor = Color.Transparent
        )
    )
}

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

@Composable
fun HamburgerButton(onClick: () -> Unit = {}) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(
            imageVector = Icons.Filled.Menu,
            contentDescription = stringResource(Res.string.menu)
        )
    }
}

@Composable
fun CloseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    IconButton(
        modifier = modifier.padding(NavigationDrawerItemDefaults.ItemPadding),
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
    navController: NavController,
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
        Column(
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            AnimatedVisibility(!isDrawerPermanent) {
                CloseButton(modifier = Modifier.padding(start = 4.dp)) { closeDrawer() }
            }
            ChatsNavigationDrawerItem(selected = navController.currentDestination?.route == "chat" && !isProfileShown) {
                navigateToChats()
            }
            NewChatNavigationDrawerItem(selected = navController.currentDestination?.route == "newChat" && !isProfileShown) {
                navigateToNewChat()
            }
            ProfileNavigationDrawerItem(selected = isProfileShown) {
                onProfileClick()
            }
        }
    }
}

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

val LocalNavBarOrientation = staticCompositionLocalOf<Orientation> {
    error("No NavBarOrientation provided")
}

expect fun getNavBarTooltipGestures(): List<GestureType>
