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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import illyan.butler.core.ui.components.ButlerDialog
import illyan.butler.core.ui.components.PlainTooltipWithContent
import illyan.butler.core.ui.getTooltipGestures
import illyan.butler.core.ui.utils.plus
import illyan.butler.domain.model.DomainChat
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.menu
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.profile
import illyan.butler.ui.auth_flow.AuthFlow
import illyan.butler.ui.chat_layout.ChatLayout
import illyan.butler.ui.chat_list.ChatList
import illyan.butler.ui.error.ErrorScreen
import illyan.butler.ui.onboard_flow.OnboardFlow
import illyan.butler.ui.profile.ProfileDialog
import illyan.butler.ui.settings.UserSettings
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Home() {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsState()
    Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)) {
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
            LaunchedEffect(state.isUserSignedIn) { currentChat = null }
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
                                    chats = state.userChats,
                                    isProfileDialogShowing = isProfileDialogShowing,
                                    setProfileDialogShowing = { isProfileDialogShowing = it },
                                    navigateToNewChat = { currentChat = null },
                                    selectChat = { currentChat = it },
                                    deleteChat = {
                                        viewModel.deleteChat(it)
                                        if (currentChat == it) currentChat = null
                                    },
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
                                            .fillMaxHeight(),
                                        drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
                                        drawerContentColor = MaterialTheme.colorScheme.onSurface,
                                        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
                                    ) {
                                        NavigationDrawerContent(
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            closeDrawer = { coroutineScope.launch { drawerState.close() } },
                                            closeButtonPadding = DrawerCloseButtonPadding,
                                            bottomContent = {
                                                NavDrawerItem(
                                                    modifier = Modifier
                                                        .systemBarsPadding()
                                                        .imePadding()
                                                        .padding(bottom = 8.dp),
                                                    onClick = { isProfileDialogShowing = true },
                                                    icon = Icons.Filled.Person,
                                                    stringResource = Res.string.profile
                                                )
                                            }
                                        ) {
                                            ChatList(
                                                chats = state.userChats,
                                                deleteChat = {
                                                    viewModel.deleteChat(it)
                                                    if (currentChat == it) currentChat = null
                                                },
                                                openChat = {
                                                    currentChat = it
                                                    coroutineScope.launch { drawerState.close() }
                                                }
                                            )
                                        }
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
                                        OnboardFlow(onTutorialDone = viewModel::setTutorialDone)
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
    navigateToNewChat: () -> Unit,
    expandNavRail: () -> Unit,
    bottomContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    NavigationRail(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            HamburgerButton(expandNavRail)
            NewChatFAB(navigateToNewChat)
        }
    ) {
        Spacer(Modifier.weight(0.5f))
        content()
        Spacer(Modifier.weight(1f))
        bottomContent()
    }
}

@Composable
private fun VerticalNavBar(
    modifier: Modifier = Modifier,
    selectChat: (String?) -> Unit = {},
    chats: List<DomainChat>,
    deleteChat: (String) -> Unit = {},
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
                navigateToNewChat = navigateToNewChat,
                expandNavRail = { navRailExpanded = true },
                bottomContent = {
                    NavRailItem(
                        modifier = Modifier.systemBarsPadding().imePadding().padding(bottom = 8.dp),
                        icon = Icons.Filled.Person,
                        stringResource = Res.string.profile,
                        onClick = { setProfileDialogShowing(true) },
                        selected = isProfileDialogShowing
                    )
                },
                content = {}
            )
        } else {
            PermanentNavigationDrawerSheet(
                modifier = modifier.statusBarsPadding(),
                selectChat = {
                    selectChat(it)
                    navRailExpanded = false
                },
                chats = chats,
                deleteChat = deleteChat,
                onProfileClick = { setProfileDialogShowing(true) },
                closeDrawer = { navRailExpanded = false },
            )
        }
    }
}

@Composable
fun NavRailItem(
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    icon: ImageVector,
    stringResource: StringResource
) {
    NavigationRailItem(
        modifier = modifier,
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
fun NavDrawerItem(
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
    chats: List<DomainChat>,
    selectChat: (String?) -> Unit = {},
    deleteChat: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    closeDrawer: () -> Unit = {},
) {
    PermanentDrawerSheet(
        modifier = modifier.widthIn(max = 280.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp),
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        NavigationDrawerContent(
            modifier = Modifier.padding(vertical = 4.dp),
            closeDrawer = closeDrawer,
            bottomContent = {
                NavDrawerItem(
                    modifier = Modifier
                        .systemBarsPadding()
                        .imePadding()
                        .padding(bottom = 8.dp),
                    onClick = onProfileClick,
                    icon = Icons.Filled.Person,
                    stringResource = Res.string.profile
                )
            }
        ) {
            ChatList(
                modifier = Modifier,
                chats = chats,
                openChat = selectChat,
                deleteChat = deleteChat
            )
        }
    }
}

private val RailCloseButtonPadding: PaddingValues @Composable get() = NavigationDrawerItemDefaults.ItemPadding + PaddingValues(start = 4.dp)
private val DrawerCloseButtonPadding = PaddingValues(start = 4.dp)

@Composable
private fun NavigationDrawerContent(
    modifier: Modifier = Modifier,
    closeDrawer: () -> Unit,
    closeButtonPadding: PaddingValues = RailCloseButtonPadding,
    bottomContent: @Composable () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    Column(modifier = modifier.fillMaxHeight()) {
        CloseButton(modifier = Modifier.padding(closeButtonPadding)) { closeDrawer() }
        content()
        Spacer(Modifier.weight(1f))
        bottomContent()
    }
}
