package illyan.butler.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowWidthSizeClass
import illyan.butler.core.ui.components.ButlerDialog
import illyan.butler.core.ui.components.PlainTooltipWithContent
import illyan.butler.core.ui.getTooltipGestures
import illyan.butler.core.ui.utils.plus
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.chats
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.menu
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.profile
import illyan.butler.ui.auth_flow.AuthFlow
import illyan.butler.ui.auth_success.AuthSuccessIcon
import illyan.butler.ui.chat_layout.ChatLayout
import illyan.butler.ui.error.ErrorScreen
import illyan.butler.ui.new_chat.NewChat
import illyan.butler.ui.profile.ProfileDialog
import illyan.butler.ui.select_host.SelectHost
import illyan.butler.ui.select_host_tutorial.SelectHostTutorial
import illyan.butler.ui.settings.UserSettings
import illyan.butler.ui.signup_tutorial.SignUpTutorial
import illyan.butler.ui.usage_tutorial.UsageTutorialScreen
import illyan.butler.ui.welcome.Welcome
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Home() {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsState()
    Surface(
//            modifier = Modifier.safeContentPadding(),
        color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
    ) {
        Column(modifier = Modifier.statusBarsPadding()) {
            var isProfileDialogShowing by rememberSaveable { mutableStateOf(false) }
            var isAuthFlowEnded by rememberSaveable { mutableStateOf(state.isUserSignedIn) }
            LaunchedEffect(state.isUserSignedIn) {
                state.isUserSignedIn?.let { isSignedIn ->
                    if (isAuthFlowEnded == null) isAuthFlowEnded = isSignedIn
                    if (!isSignedIn) isAuthFlowEnded = false
                }
                isProfileDialogShowing = false
            }

            LaunchedEffect(state.isTutorialDone) {
                if (state.isUserSignedIn == true && state.isTutorialDone == true) isAuthFlowEnded = true
            }
            val userFlow = remember(
                state.isTutorialDone,
                isAuthFlowEnded,
                state.isUserSignedIn,
                isProfileDialogShowing
            ) {
                if (state.isTutorialDone == true) {
                    if (isAuthFlowEnded == true && state.isUserSignedIn == true && isProfileDialogShowing) null else DialogUserFlow.Auth
                } else DialogUserFlow.OnBoarding
            }

            val profileNavController = rememberNavController()
            val animationTime = 200
            ButlerDialog(
                isDialogOpen = isProfileDialogShowing,
                isDialogFullscreen = false,
                onDismissDialog = { isProfileDialogShowing = false },
                onDialogClosed = {},
                navController = profileNavController
            ) {
                NavHost(
                    navController = profileNavController,
                    contentAlignment = Alignment.Center,
                    sizeTransform = { SizeTransform(clip = false) },
                    startDestination = "profile",
                    enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
                    popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
                    exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
                    popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
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
            val navigationSuiteLayout = remember(windowSizeClass) {
                when (windowSizeClass.windowWidthSizeClass) {
                    WindowWidthSizeClass.COMPACT -> NavigationSuiteType.NavigationDrawer
                    WindowWidthSizeClass.MEDIUM -> NavigationSuiteType.NavigationRail
                    else -> NavigationSuiteType.NavigationDrawer
                }
            }
            val isVerticalNavBarCompact = remember(windowSizeClass.windowWidthSizeClass) {
                windowSizeClass.windowWidthSizeClass != WindowWidthSizeClass.EXPANDED
            }
            var currentChat by rememberSaveable { mutableStateOf<String?>(null) }
            val isCompact = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
            NavigationSuiteScaffoldLayout(
                layoutType = navigationSuiteLayout,
                navigationSuite = {
                    AnimatedContent(
                        targetState = navigationSuiteLayout,
                        transitionSpec = { slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut() }
                    ) { _ ->
                        if (state.isUserSignedIn == true) {
                            AnimatedVisibility(
                                visible = !isCompact,
                                enter = slideInHorizontally { -it } + fadeIn(),
                                exit = slideOutHorizontally { it } + fadeOut()
                            ) {
                                VerticalNavBar(
                                    modifier = Modifier.imePadding().navigationBarsPadding().displayCutoutPadding(),
                                    compact = isVerticalNavBarCompact,
                                    selectedChat = currentChat,
                                    isProfileDialogShowing = isProfileDialogShowing,
                                    setProfileDialogShowing = { isProfileDialogShowing = it },
                                    navigateToNewChat = { currentChat = null }
                                )
                            }
                        }
                    }
                }
            ) {
                Surface(
                    tonalElevation = 0.dp,
                    shape = RoundedCornerShape(topStart = if (isCompact) 0.dp else 24.dp)
                ) {
                    AnimatedContent(userFlow to state.isUserSignedIn) { (flow, userSignedIn) ->
                        if (userSignedIn == true) {
                            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                            LaunchedEffect(isCompact) {
                                if (!isCompact) drawerState.close()
                            }
                            val coroutineScope = rememberCoroutineScope()
                            ModalNavigationDrawer(
                                drawerState = drawerState,
                                gesturesEnabled = isCompact,
                                drawerContent = {
                                    ModalDrawerSheet(
                                        modifier = Modifier
                                            .widthIn(max = 280.dp)
                                            .fillMaxHeight()
                                            .clip(RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)),
                                        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
                                    ) {
                                        NavigationDrawerContent(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            selectedChat = currentChat,
                                            isProfileShown = isProfileDialogShowing,
                                            closeDrawer = { coroutineScope.launch { drawerState.close() } },
                                            navigateToNewChat = { currentChat = null },
                                            onProfileClick = { isProfileDialogShowing = true },
                                            closeButtonPadding = DrawerCloseButtonPadding
                                        )
                                    }
                                },
                            ) {
                                ChatLayout(
                                    currentChat = currentChat,
                                    selectChat = { currentChat = it },
                                    navigationIcon = {
                                        if (isCompact) {
                                            HamburgerButton {
                                                coroutineScope.launch { drawerState.open() }
                                            }
                                        }
                                    }
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                when (flow) {
                                    DialogUserFlow.Auth -> {
                                        AuthFlow(authSuccessEnded = { isAuthFlowEnded = true })
                                    }

                                    DialogUserFlow.OnBoarding -> {
                                        val onBoardingNavController = rememberNavController()
                                        NavHost(
                                            navController = onBoardingNavController,
                                            contentAlignment = Alignment.Center,
                                            sizeTransform = { SizeTransform(clip = false) },
                                            startDestination = "welcome",
                                            enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
                                            popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
                                            exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
                                            popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
                                        ) {
                                            composable("welcome") {
                                                Welcome {
                                                    onBoardingNavController.navigate("selectHostTutorial")
                                                }
                                            }
                                            composable("selectHostTutorial") {
                                                SelectHostTutorial {
                                                    onBoardingNavController.navigate("selectHost")
                                                }
                                            }
                                            composable("selectHost") {
                                                SelectHost {
                                                    onBoardingNavController.navigate("signUpTutorial")
                                                }
                                            }
                                            composable("signUpTutorial") {
                                                SignUpTutorial {
                                                    onBoardingNavController.navigate("auth")
                                                }
                                            }
                                            composable("auth") {
                                                AuthFlow(
                                                    authSuccessEnded = {
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
                                                UsageTutorialScreen { viewModel.setTutorialDone() }
                                            }
                                        }
                                    }
                                    else -> {}
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
private fun HomeNavRail(
    modifier: Modifier = Modifier,
    selectedChat: String?,
    isProfileDialogShowing: Boolean,
    setProfileDialogShowing: (Boolean) -> Unit,
    navigateToNewChat: () -> Unit,
    expandNavRail: () -> Unit
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        header = {
            HamburgerButton(expandNavRail)
            NewChatFAB(navigateToNewChat)
        }
    ) {
        Spacer(Modifier.weight(0.5f))
        NewChatNavigationRailItem(selected = selectedChat == null && !isProfileDialogShowing) {
            navigateToNewChat()
        }
        ProfileNavigationRailItem(selected = isProfileDialogShowing) {
            setProfileDialogShowing(true)
        }
        Spacer(Modifier.weight(1f))
    }
}


@Composable
private fun VerticalNavBar(
    modifier: Modifier = Modifier,
    selectedChat: String?,
    compact: Boolean,
    isProfileDialogShowing: Boolean,
    setProfileDialogShowing: (Boolean) -> Unit,
    navigateToNewChat: () -> Unit,
) {
    var navRailExpanded by rememberSaveable(compact) { mutableStateOf(!compact) }
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
            HomeNavRail(
                modifier = modifier.statusBarsPadding(),
                selectedChat = selectedChat,
                isProfileDialogShowing = isProfileDialogShowing,
                setProfileDialogShowing = setProfileDialogShowing,
                navigateToNewChat = navigateToNewChat,
                expandNavRail = { navRailExpanded = true }
            )
        } else {
            PermanentNavigationDrawerSheet(
                modifier = modifier.statusBarsPadding(),
                selectedChat = selectedChat,
                isProfileShown = isProfileDialogShowing,
                onProfileClick = { setProfileDialogShowing(true) },
                navigateToNewChat = navigateToNewChat,
                closeDrawer = { navRailExpanded = false },
            )
        }
    }
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
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = stringResource(Res.string.close)
        )
    }
}

@Composable
private fun PermanentNavigationDrawerSheet(
    modifier: Modifier = Modifier,
    selectedChat: String?,
    isProfileShown: Boolean = false,
    onProfileClick: () -> Unit = {},
    closeDrawer: () -> Unit = {},
    navigateToNewChat: () -> Unit = {}
) {
    PermanentDrawerSheet(
        modifier = modifier.widthIn(max = 280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
    ) {
        NavigationDrawerContent(
            modifier = Modifier.padding(vertical = 4.dp),
            selectedChat = selectedChat,
            isProfileShown = isProfileShown,
            closeDrawer = closeDrawer,
            navigateToNewChat = navigateToNewChat,
            onProfileClick = onProfileClick
        )
    }
}

private val RailCloseButtonPadding: PaddingValues @Composable get() = NavigationDrawerItemDefaults.ItemPadding + PaddingValues(start = 4.dp)
private val DrawerCloseButtonPadding = PaddingValues(start = 4.dp)

@Composable
private fun NavigationDrawerContent(
    modifier: Modifier = Modifier,
    selectedChat: String?,
    isProfileShown: Boolean,
    closeDrawer: () -> Unit,
    navigateToNewChat: () -> Unit,
    onProfileClick: () -> Unit,
    closeButtonPadding: PaddingValues = RailCloseButtonPadding
) {
    Column(modifier = modifier) {
        CloseButton(modifier = Modifier.padding(closeButtonPadding)) { closeDrawer() }
        NewChatNavigationDrawerItem(selected = selectedChat != null && !isProfileShown) {
            navigateToNewChat()
        }
        ProfileNavigationDrawerItem(selected = isProfileShown) {
            onProfileClick()
        }
    }
}
