package illyan.butler.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowWidthSizeClass
import illyan.butler.core.ui.components.ButlerDialog
import illyan.butler.core.ui.components.PlainTooltipWithContent
import illyan.butler.core.ui.getTooltipGestures
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.menu
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.profile
import illyan.butler.ui.auth.AuthScreen
import illyan.butler.ui.auth_success.AuthSuccessIcon
import illyan.butler.ui.chat_layout.ChatScreen
import illyan.butler.ui.error.ErrorScreen
import illyan.butler.ui.new_chat.NewChatScreen
import illyan.butler.ui.permission.PermissionRequestScreen
import illyan.butler.ui.profile.ProfileDialog
import illyan.butler.ui.select_host.SelectHostScreen
import illyan.butler.ui.select_host_tutorial.SelectHostTutorialScreen
import illyan.butler.ui.settings.UserSettings
import illyan.butler.ui.signup_tutorial.SignUpTutorialScreen
import illyan.butler.ui.usage_tutorial.UsageTutorialScreen
import illyan.butler.ui.welcome.WelcomeScreen
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
private data class ChatDestination(val id: String? = null)

@Serializable
data object NewChatDestination

@Composable
fun HomeScreen() {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsState()
    Surface(
//            modifier = Modifier.safeContentPadding(),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            var isProfileDialogShowing by rememberSaveable { mutableStateOf(false) }
            var isAuthFlowEnded by remember { mutableStateOf(state.isUserSignedIn) }
            LaunchedEffect(state.isUserSignedIn) {
                state.isUserSignedIn?.let { isSignedIn ->
                    if (isAuthFlowEnded == null) isAuthFlowEnded = isSignedIn
                    if (!isSignedIn) isAuthFlowEnded = false
                }
                isProfileDialogShowing = false
            }
            val isDialogOpen = rememberSaveable(
                isAuthFlowEnded,
                state.isTutorialDone,
                isProfileDialogShowing
            ) { isAuthFlowEnded != true || state.isTutorialDone == false || isProfileDialogShowing }

            var isDialogClosedAfterTutorial by rememberSaveable { mutableStateOf(state.isTutorialDone) }
            LaunchedEffect(state.isTutorialDone) {
                if (isDialogClosedAfterTutorial == null) isDialogClosedAfterTutorial =
                    state.isTutorialDone
                if (state.isTutorialDone == false) isDialogClosedAfterTutorial = false
                if (state.isUserSignedIn == true && state.isTutorialDone == true) isAuthFlowEnded =
                    true
            }
            val userFlow = remember(
                isDialogOpen,
                state.isTutorialDone,
                isDialogClosedAfterTutorial,
                isAuthFlowEnded,
                state.isUserSignedIn,
                isProfileDialogShowing
            ) {
                if (!isDialogOpen) null
                else if (state.isTutorialDone == true && isDialogClosedAfterTutorial == true) {
                    if (isAuthFlowEnded == true && state.isUserSignedIn == true && isProfileDialogShowing) DialogUserFlow.Profile else DialogUserFlow.Auth
                } else DialogUserFlow.OnBoarding
            }

            val onBoardingNavController = rememberNavController()
            val profileNavController = rememberNavController()
            val authNavController = rememberNavController()
            var currentNavController by remember { mutableStateOf<NavHostController?>(null) }
            LaunchedEffect(userFlow) {
                Napier.d("Current dialog flow: $userFlow")
                currentNavController = when (userFlow) {
                    DialogUserFlow.Auth -> authNavController
                    DialogUserFlow.OnBoarding -> onBoardingNavController
                    DialogUserFlow.Profile -> profileNavController
                    null -> currentNavController
                }
            }

            ButlerDialog(
                isDialogOpen = isDialogOpen,
                isDialogFullscreen = state.isUserSignedIn != true || state.isTutorialDone == false,
                onDismissDialog = {
                    if (state.isUserSignedIn == true) isAuthFlowEnded = true
                    isProfileDialogShowing = false
                },
                onDialogClosed = {
                    if (state.isTutorialDone == true) isDialogClosedAfterTutorial = true
                },
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
                                contentAlignment = Alignment.Center,
                                sizeTransform = { SizeTransform(clip = false) },
                                startDestination = "welcome",
                                enterTransition = {
                                    slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(
                                        tween(animationTime)
                                    )
                                },
                                popEnterTransition = {
                                    slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(
                                        tween(animationTime)
                                    )
                                },
                                exitTransition = {
                                    slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(
                                        tween(animationTime)
                                    )
                                },
                                popExitTransition = {
                                    slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(
                                        tween(animationTime)
                                    )
                                }
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
                                            onBoardingNavController.navigate("usageTutorial") {
                                                launchSingleTop = true
                                            }
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
                                contentAlignment = Alignment.Center,
                                sizeTransform = { SizeTransform(clip = false) },
                                startDestination = "profile",
                                enterTransition = {
                                    slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(
                                        tween(animationTime)
                                    )
                                },
                                popEnterTransition = {
                                    slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(
                                        tween(animationTime)
                                    )
                                },
                                exitTransition = {
                                    slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(
                                        tween(animationTime)
                                    )
                                },
                                popExitTransition = {
                                    slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(
                                        tween(animationTime)
                                    )
                                }
                            ) {
                                composable("profile") {
                                    ProfileDialog {
                                        profileNavController.navigate("settings")
                                    }
                                }
                                composable("settings") {
                                    UserSettings()
                                }
                            }
                        }

                        null -> {}
                    }
                }
            }

            val numberOfErrors = remember(
                state.appErrors,
                state.serverErrors
            ) { state.appErrors.size + state.serverErrors.size }
            ButlerDialog(
                modifier = Modifier.zIndex(1f),
                isDialogOpen = numberOfErrors > 0,
                isDialogFullscreen = false,
                onDismissDialog = viewModel::removeLastError,
            ) {
                ErrorScreen(
                    cleanError = viewModel::clearError,
                    appErrors = state.appErrors,
                    serverErrors = state.serverErrors
                )
            }
            // Index is rememberSaveable, Screen is probably not.
            val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
            val isNavBarCompact = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
            val navBarOrientation = remember(isNavBarCompact) {
                if (isNavBarCompact) Orientation.Horizontal else Orientation.Vertical
            }
            val navigationSuiteLayout = remember(windowSizeClass) {
                when (windowSizeClass.windowWidthSizeClass) {
                    WindowWidthSizeClass.COMPACT -> NavigationSuiteType.NavigationBar
                    WindowWidthSizeClass.MEDIUM -> NavigationSuiteType.NavigationRail
                    else -> NavigationSuiteType.NavigationDrawer
                }
            }
            val homeNavController = rememberNavController()
            val isVerticalNavBarCompact = remember(windowSizeClass.windowWidthSizeClass) {
                windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.EXPANDED
            }
            val navigateToChats = {
                if (homeNavController.currentDestination?.hasRoute<ChatDestination>() != true) {
                    homeNavController.navigate(ChatDestination()) {
                        popUpTo(homeNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                    }
                }
            }
            val navigateToNewChat = {
                if (homeNavController.currentDestination?.hasRoute<NewChatDestination>() != true) {
                    homeNavController.navigate(NewChatDestination) {
                        popUpTo(homeNavController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
            NavigationSuiteScaffoldLayout(
                layoutType = navigationSuiteLayout,
                navigationSuite = {
                    AnimatedContent(
                        targetState = navigationSuiteLayout,
                        transitionSpec = {
                            val enter = if (targetState == NavigationSuiteType.NavigationBar) {
                                slideInVertically { -it } + fadeIn()
                            } else {
                                slideInHorizontally { -it } + fadeIn()
                            }
                            val exit = if (initialState == NavigationSuiteType.NavigationBar) {
                                slideOutVertically { it } + fadeOut()
                            } else {
                                slideOutHorizontally { it } + fadeOut()
                            }
                            enter togetherWith exit
                        }
                    ) { type ->
                        if (type == NavigationSuiteType.NavigationBar) {
                            HorizontalNavBar(
                                modifier = Modifier.navigationBarsPadding(),
                                navController = homeNavController,
                                isProfileDialogShowing = isProfileDialogShowing,
                                setProfileDialogShowing = { isProfileDialogShowing = it },
                                navigateToChats = navigateToChats,
                                navigateToNewChat = navigateToNewChat
                            )
                        } else {
                            VerticalNavBar(
                                modifier = Modifier.imePadding().navigationBarsPadding().displayCutoutPadding(),
                                navController = homeNavController,
                                compact = isVerticalNavBarCompact,
                                isProfileDialogShowing = isProfileDialogShowing,
                                setProfileDialogShowing = { isProfileDialogShowing = it },
                                navigateToChats = navigateToChats,
                                navigateToNewChat = navigateToNewChat
                            )
                        }
                    }
                }
            ) {
                HomeContent(navBarOrientation) {
                    NavHost(
                        navController = homeNavController,
                        startDestination = ChatDestination()
                    ) {
                        composable<ChatDestination> {
                            ChatScreen(currentChat = it.toRoute<ChatDestination>().id)
                        }
                        composable<NewChatDestination> {
                            NewChatScreen { chatId ->
                                homeNavController.navigate(ChatDestination(chatId)) {
                                    // Pop up to the start destination of the graph to
                                    // avoid building up a large stack of destinations
                                    // on the back stack as users select items
                                    popUpTo(homeNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    // Avoid multiple copies of the same destination when
                                    // reselecting the same item
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HorizontalNavBar(
    modifier: Modifier = Modifier,
    navController: NavController,
    isProfileDialogShowing: Boolean,
    setProfileDialogShowing: (Boolean) -> Unit,
    navigateToChats: () -> Unit,
    navigateToNewChat: () -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    ) {
        ChatsNavigationBarItem(selected = backStackEntry?.destination?.hasRoute<ChatDestination>() == true && !isProfileDialogShowing) {
            navigateToChats()
        }
        NewChatNavigationBarItem(selected = backStackEntry?.destination?.hasRoute<NewChatDestination>() == true && !isProfileDialogShowing) {
            navigateToNewChat()
        }
        ProfileNavigationBarItem(selected = isProfileDialogShowing) {
            setProfileDialogShowing(true)
        }
    }
}

@Composable
private fun VerticalNavBar(
    modifier: Modifier = Modifier,
    compact: Boolean,
    navController: NavController,
    isProfileDialogShowing: Boolean,
    setProfileDialogShowing: (Boolean) -> Unit,
    navigateToChats: () -> Unit,
    navigateToNewChat: () -> Unit,
) {
    var navRailExpanded by rememberSaveable(compact) { mutableStateOf(!compact) }
    val backStackEntry by navController.currentBackStackEntryAsState()
    AnimatedContent(
        targetState = !navRailExpanded,
        transitionSpec = {
            if (targetState) {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            } else {
                slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
            }
        }
    ) { isCompact ->
        if (isCompact) {
            NavigationRail(
                modifier = modifier.statusBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                header = {
                    HamburgerButton { navRailExpanded = true }
                    NewChatFAB { navigateToNewChat() }
                }
            ) {
                Spacer(Modifier.weight(0.5f))
                ChatsNavigationRailItem(selected = backStackEntry?.destination?.hasRoute<ChatDestination>() == true && !isProfileDialogShowing) {
                    navigateToChats()
                }
                NewChatNavigationRailItem(selected = backStackEntry?.destination?.hasRoute<NewChatDestination>() == true && !isProfileDialogShowing) {
                    navigateToNewChat()
                }
                ProfileNavigationRailItem(selected = isProfileDialogShowing) {
                    setProfileDialogShowing(true)
                }
                Spacer(Modifier.weight(1f))
            }
        } else {
            NavigationDrawerContent(
                modifier = modifier.statusBarsPadding(),
                navController = navController,
                isProfileShown = isProfileDialogShowing,
                onProfileClick = { setProfileDialogShowing(true) },
                navigateToChats = navigateToChats,
                navigateToNewChat = navigateToNewChat,
                closeDrawer = { navRailExpanded = false },
                isDrawerPermanent = false
            )
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
        enabledGestures = getTooltipGestures(),
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
    modifier: Modifier = Modifier,
    navController: NavController,
    isProfileShown: Boolean = false,
    onProfileClick: () -> Unit = {},
    closeDrawer: () -> Unit = {},
    navigateToChats: () -> Unit = {},
    navigateToNewChat: () -> Unit = {},
    isDrawerPermanent: Boolean = false
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val destinationRoute = remember(backStackEntry) { backStackEntry?.destination?.route }
    PermanentDrawerSheet(
        modifier = modifier.widthIn(max = 280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            AnimatedVisibility(!isDrawerPermanent) {
                CloseButton(modifier = Modifier.padding(start = 4.dp)) { closeDrawer() }
            }
            ChatsNavigationDrawerItem(selected = destinationRoute == "chat" && !isProfileShown) {
                navigateToChats()
            }
            NewChatNavigationDrawerItem(selected = destinationRoute == "newChat" && !isProfileShown) {
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
