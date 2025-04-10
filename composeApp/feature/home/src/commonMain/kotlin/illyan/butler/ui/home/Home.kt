package illyan.butler.ui.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldLayout
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import com.mikepenz.aboutlibraries.Libs
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import illyan.butler.core.ui.components.ButlerDialog
import illyan.butler.core.ui.components.PlainTooltipWithContent
import illyan.butler.core.ui.components.mediumDialogWidth
import illyan.butler.core.ui.getTooltipGestures
import illyan.butler.core.ui.utils.BackHandler
import illyan.butler.core.ui.utils.plus
import illyan.butler.domain.model.DomainChat
import illyan.butler.domain.model.DomainError
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.create_new_chat
import illyan.butler.generated.resources.menu
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_chats
import illyan.butler.generated.resources.profile
import illyan.butler.ui.chat_layout.ChatLayout
import illyan.butler.ui.chat_list.ChatList
import illyan.butler.ui.error.ErrorDialogContent
import illyan.butler.ui.error.ErrorSnackbarHost
import illyan.butler.ui.onboard_flow.OnboardFlow
import illyan.butler.ui.profile.about.AboutDialogContent
import illyan.butler.ui.profile.dialog.ProfileDialog
import illyan.butler.ui.profile.settings.UserSettings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun Home(
    libraries: State<Libs?>
) {
    val viewModel = koinViewModel<HomeViewModel>()
    val state by viewModel.state.collectAsState()
    var isProfileDialogShowing by rememberSaveable(state.signedInUserId) { mutableStateOf(false) }
    var initialLoadingDelayPassed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        initialLoadingDelayPassed = true
    }
    var isAuthFlowEnded by rememberSaveable(
        state.signedInUserId,
        state.credentials,
        initialLoadingDelayPassed
    ) { mutableStateOf(if (initialLoadingDelayPassed) (state.signedInUserId != null || state.credentials.isNotEmpty()) else null) }
    val profileNavController = rememberNavController()
    val animationTime = 200
    ButlerDialog(
        isDialogOpen = isProfileDialogShowing,
        isDialogFullscreen = profileNavController.currentBackStackEntry?.destination?.route == "libraries",
        onDismissDialog = {
            Napier.d { "Dialog dismissed, previous entry: ${profileNavController.previousBackStackEntry?.destination}" }
            if (profileNavController.previousBackStackEntry != null) {
                Napier.d { "Navigating up" }
                profileNavController.navigateUp()
            } else {
                Napier.d { "Closing dialog" }
                isProfileDialogShowing = false
            }
        },
        onDialogClosed = {},
    ) {
        NavHost(
            navController = profileNavController,
            contentAlignment = Alignment.Center,
            sizeTransform = { SizeTransform(clip = false, sizeAnimationSpec = { _, _ -> tween(animationTime) }) },
            startDestination = "profile",
            enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
            popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
            exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
            popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
        ) {
            composable("profile") {
                ProfileDialog(
                    onShowSettingsScreen = { profileNavController.navigate("settings") },
                    onLogin = {
                        isAuthFlowEnded = false
                        isProfileDialogShowing = false
                    },
                    onClose = {
                        isProfileDialogShowing = false
                    },
                    onAbout = { profileNavController.navigate("about") }
                )
            }
            composable("settings") {
                UserSettings()
            }
            composable("about") {
                AboutDialogContent(
                    modifier = Modifier.mediumDialogWidth()
                    // FIXME: LibrariesContainer is not compatible in Compose Multiplatform 1.8.0 yet.
//                    onNavigateToLibraries = { profileNavController.navigate("libraries") },
                )
            }
            composable("libraries") {
                val libs by libraries
                LibrariesContainer(
                    modifier = Modifier.fillMaxSize(),
                    libraries = libs
                )
            }
        }
    }

    val numberOfDialogErrors = state.errors.filter { it !is DomainError.Event.Simple }.size
    ButlerDialog(
        modifier = Modifier.zIndex(1f),
        isDialogOpen = numberOfDialogErrors > 0,
        isDialogFullscreen = false,
        onDismissDialog = viewModel::removeLastError,
    ) {
        ErrorDialogContent(
            cleanError = viewModel::clearError,
            errors = state.errors
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
    var currentChat by rememberSaveable(state.signedInUserId) { mutableStateOf<String?>(null) }
    val isCompact = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val isExpanded = windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED
    Scaffold(
        snackbarHost = { ErrorSnackbarHost(
            modifier = Modifier.widthIn(max = 420.dp),
            errors = state.errors.mapNotNull { it as? DomainError.Event.Simple },
            cleanError = viewModel::clearError,
        ) },
        containerColor = if (isAuthFlowEnded == true) MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp) else MaterialTheme.colorScheme.surface
    ) { _ ->
        NavigationSuiteScaffoldLayout(
            layoutType = navigationSuiteLayout,
            navigationSuite = {
                AnimatedContent(
                    targetState = navigationSuiteLayout,
                    transitionSpec = {
                        slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut() using SizeTransform(clip = true)
                    }
                ) { _ ->
                    AnimatedVisibility(
                        visible = !isCompact && isAuthFlowEnded == true,
                        enter = slideInHorizontally { -it } + fadeIn() + expandHorizontally(expandFrom = Alignment.Start),
                        exit = slideOutHorizontally { -it } + fadeOut() + shrinkHorizontally(shrinkTowards = Alignment.Start)
                    ) {
                        VerticalNavBar(
                            modifier = Modifier.imePadding().navigationBarsPadding().displayCutoutPadding(),
                            compact = isVerticalNavBarCompact,
                            userChats = state.userChats,
                            deviceChats = state.deviceChats,
                            isProfileDialogShowing = isProfileDialogShowing,
                            setProfileDialogShowing = { isProfileDialogShowing = it },
                            navigateToNewChat = { currentChat = null },
                            selectChat = { currentChat = it },
                            deleteChat = {
                                viewModel.deleteChat(it)
                                if (currentChat == it) currentChat = null
                            },
                            currentChat = currentChat,
                            isExpanded = isExpanded
                        )
                    }
                }
            }
        ) {
            Surface(modifier = Modifier.fillMaxSize(), tonalElevation = 0.dp) {
                AnimatedContent(
                    targetState = isAuthFlowEnded,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { isHome ->
                    if (isHome == null) return@AnimatedContent
                    if (isHome) {
                        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                        var drawerContentWidthInPixels by remember { mutableStateOf(0) }
                        val drawerOpenRatio = remember(
                            drawerState.currentOffset,
                            drawerContentWidthInPixels
                        ) {
                            (((drawerState.currentOffset / drawerContentWidthInPixels) + 1).takeIf { !it.isNaN() } ?: 0f).coerceIn(0f, 1f)
                        }
                        val coroutineScope = rememberCoroutineScope()
                        LaunchedEffect(isCompact) {
                            if (!isCompact) drawerState.close()
                        }
                        BackHandler(drawerState.isOpen) {
                            coroutineScope.launch { drawerState.close() }
                        }
                        DismissibleNavigationDrawer(
                            drawerState = drawerState,
                            gesturesEnabled = isCompact,
                            drawerContent = {
                                CompositionLocalProvider(LocalAbsoluteTonalElevation provides LocalAbsoluteTonalElevation.current + 2.dp) {
                                    DismissibleDrawerSheet(
                                        modifier = Modifier
                                            .widthIn(max = 280.dp)
                                            .fillMaxHeight(),
                                        drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(LocalAbsoluteTonalElevation.current),
                                        drawerContentColor = MaterialTheme.colorScheme.onSurface,
                                        windowInsets = WindowInsets.systemBars.union(WindowInsets.displayCutout),
                                    ) {
                                        NavigationDrawerContent(
                                            modifier = Modifier.padding(vertical = 8.dp).onSizeChanged {
                                                drawerContentWidthInPixels = it.width
                                            },
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
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                NewChatFABExtended(
                                                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                                                    onClick = {
                                                        currentChat = null
                                                        coroutineScope.launch { drawerState.close() }
                                                    }
                                                )

                                                AnimatedVisibility(
                                                    visible = (state.userChats + state.deviceChats).isEmpty(),
                                                    enter = fadeIn(),
                                                    exit = fadeOut()
                                                ) {
                                                    EmptyChatNavDrawerItem()
                                                }

                                                val chats = remember(state.userChats, state.deviceChats, state.lastInteractionTimestampForChat) {
                                                    (state.userChats + state.deviceChats).sortedByDescending {
                                                        state.lastInteractionTimestampForChat[it.id!!] ?: it.created
                                                    }
                                                }
                                                ChatList(
                                                    chats = chats,
                                                    deleteChat = {
                                                        viewModel.deleteChat(it)
                                                        if (currentChat == it) currentChat = null
                                                    },
                                                    openChat = {
                                                        currentChat = it
                                                        coroutineScope.launch { drawerState.close() }
                                                    },
                                                    selectedChat = currentChat,
                                                    deviceOnlyChatIds = state.deviceChats.map { it.id!! }
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                        ) {
                            Surface(color = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)) {
                                ChatLayout(
                                    modifier = Modifier.fillMaxSize().clip(
                                        RoundedCornerShape(
                                            topStart = if (isCompact) 24.dp * drawerOpenRatio else 24.dp,
                                            bottomStart = if (isCompact) 24.dp * drawerOpenRatio else 24.dp
                                        )
                                    ),
                                    currentChat = currentChat,
                                    selectChat = { currentChat = it },
                                    navigationIcon = {
                                        if (isCompact) {
                                            HamburgerButton {
                                                coroutineScope.launch {
                                                    if (drawerState.isOpen) drawerState.close() else drawerState.open()
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            OnboardFlow(
                                authSuccessEnded = { isAuthFlowEnded = true },
                            )
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
    isExpanded: Boolean,
    userChats: List<DomainChat>,
    deviceChats: List<DomainChat>,
    currentChat: String?,
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
                navigateToNewChat = { navigateToNewChat(); navRailExpanded = false },
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
            HomePermanentNavigationDrawerSheet(
                modifier = modifier.statusBarsPadding(),
                selectChat = {
                    selectChat(it)
                    if (!isExpanded) navRailExpanded = false
                },
                userChats = userChats,
                deviceChats = deviceChats,
                deleteChat = deleteChat,
                onProfileClick = { setProfileDialogShowing(true) },
                navigateToNewChat = { navigateToNewChat(); navRailExpanded = false },
                closeDrawer = { navRailExpanded = false },
                currentChat = currentChat
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
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(Res.string.new_chat)
            )
        }
    }
}

@Composable
fun NewChatFABExtended(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        modifier = modifier,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = stringResource(Res.string.new_chat)
            )
        },
        text = { Text(stringResource(Res.string.new_chat)) }
    )
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
fun MenuCloseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    IconButton(
        modifier = modifier,
        onClick = onClick
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.MenuOpen,
            contentDescription = stringResource(Res.string.close)
        )
    }
}

@Composable
private fun HomePermanentNavigationDrawerSheet(
    modifier: Modifier = Modifier,
    userChats: List<DomainChat>,
    deviceChats: List<DomainChat>,
    currentChat: String?,
    selectChat: (String?) -> Unit = {},
    deleteChat: (String) -> Unit = {},
    onProfileClick: () -> Unit = {},
    navigateToNewChat: () -> Unit,
    closeDrawer: () -> Unit = {},
) {
    CompositionLocalProvider(LocalAbsoluteTonalElevation provides LocalAbsoluteTonalElevation.current + 2.dp) {
        PermanentDrawerSheet(
            modifier = modifier.widthIn(max = 280.dp),
            drawerContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(LocalAbsoluteTonalElevation.current),
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    NewChatFABExtended(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        onClick = navigateToNewChat
                    )
                    AnimatedVisibility(
                        visible = (deviceChats + userChats).isEmpty(),
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        EmptyChatNavDrawerItem()
                    }
                    ChatList(
                        modifier = Modifier,
                        chats = userChats + deviceChats,
                        deviceOnlyChatIds = deviceChats.map { it.id!! },
                        openChat = selectChat,
                        deleteChat = deleteChat,
                        selectedChat = currentChat,
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyChatNavDrawerItem(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(imageVector = Icons.Rounded.ArrowUpward, contentDescription = null)
        Text(
            text = stringResource(Res.string.create_new_chat),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = stringResource(Res.string.no_chats),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.weight(3f))
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
        MenuCloseButton(modifier = Modifier.padding(closeButtonPadding)) { closeDrawer() }
        Column(
            modifier = Modifier.weight(1f),
        ) { content() }
        bottomContent()
    }
}
