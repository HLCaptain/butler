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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.profile
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.arbitrary.ArbitraryScreen
import illyan.butler.ui.auth.AuthScreen
import illyan.butler.ui.chat_layout.ChatScreen
import illyan.butler.ui.components.ButlerErrorDialogContent
import illyan.butler.ui.components.GestureType
import illyan.butler.ui.components.RichTooltipWithContent
import illyan.butler.ui.components.MenuButton
import illyan.butler.ui.components.PlainTooltipWithContent
import illyan.butler.ui.dialog.ButlerDialog
import illyan.butler.ui.new_chat.NewChatScreen
import illyan.butler.ui.onboarding.OnBoardingScreen
import illyan.butler.ui.profile.ProfileDialogScreen
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.seconds

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
                    val errorScreen by remember { derivedStateOf {
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
                    } }
                    ButlerDialog(
                        modifier = Modifier.zIndex(1f),
                        startScreens = listOf(errorScreen),
                        isDialogOpen = numberOfErrors > 0,
                        isDialogFullscreen = false,
                        onDismissDialog = screenModel::removeLastError
                    )
                }

                var navigator by remember { mutableStateOf<Navigator?>(null) }
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
                val columnContent: @Composable (@Composable () -> Unit) -> Unit = { content -> Column { content() } }
                val rowContent: @Composable (@Composable () -> Unit) -> Unit = { content -> Row { content() } }
                val layoutComposable by remember { derivedStateOf {
                    if (navBarOrientation == Orientation.Vertical) columnContent else rowContent
                } }
                LaunchedEffect(width, height) {
                    windowWidth = width
                    Napier.v("Window size: $width x $height, navBarOnSide: $navBarOrientation, layoutComposable: $layoutComposable")
                }
                val navBar = @Composable {
                    navigator?.let {
                        if (navBarOrientation == Orientation.Vertical) {
                            VerticalNavBar(
                                isCompact = isNavBarCompact,
                                onProfileClick = { isProfileDialogShowing = true },
                                onNewChatClick = { NewChatScreen { navigator?.replaceAll(ChatScreen()) } },
                                onChatsClick = { navigator?.replaceAll(ChatScreen()) }
                            )
                        } else {
                            HorizontalNavBar(
                                isCompact = isNavBarCompact,
                                onProfileClick = { isProfileDialogShowing = true },
                                onNewChatClick = { NewChatScreen { navigator?.replaceAll(ChatScreen()) } },
                                onChatsClick = { navigator?.replaceAll(ChatScreen()) }
                            )
                        }
                    }
                }
                layoutComposable {
                    AnimatedVisibility(navigator != null && navBarOrientation != Orientation.Vertical) {
                        navBar()
                    }
                    AnimatedVisibility(navigator != null && navBarOrientation == Orientation.Vertical) {
                        navBar()
                    }
                    Box(
                        modifier = Modifier.weight(1f, fill = true)
                    ) {
                        Navigator(ChatScreen()) {
                            LaunchedEffect(Unit) {
                                navigator = it
                            }
                            CurrentScreen()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
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

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
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

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
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
