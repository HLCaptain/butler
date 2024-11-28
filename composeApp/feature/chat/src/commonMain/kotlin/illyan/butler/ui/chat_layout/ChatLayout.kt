package illyan.butler.ui.chat_layout

import androidx.compose.animation.AnimatedContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.AnimatedPane
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.ThreePaneScaffoldRole
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.window.core.layout.WindowWidthSizeClass
import dev.chrisbanes.haze.LocalHazeStyle
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import illyan.butler.core.ui.utils.BackHandler
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.back
import illyan.butler.ui.chat_detail.ChatDetail
import illyan.butler.ui.chat_detail.ChatDetailViewModel
import illyan.butler.ui.chat_list.ChatList
import illyan.butler.ui.chat_list.ChatListViewModel
import illyan.butler.ui.new_chat.NewChat
import io.github.aakira.napier.Napier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun ChatLayout(
    currentChat: String?,
    selectChat: (String?) -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String?>(
        scaffoldDirective = calculatePaneScaffoldDirective(currentWindowAdaptiveInfo()).copy(
            maxHorizontalPartitions = if (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) 1 else 2
        ),
    )
    BackHandler(navigator.canNavigateBack()) {
        Napier.d("ChatLayout: BackHandler")
        selectChat(null)
        navigator.navigateBack()
    }
    LaunchedEffect(currentChat) {
        Napier.d("ChatLayout: currentChat=$currentChat, currentDestination.pane=${navigator.currentDestination?.pane}, canNavigateBack=${navigator.canNavigateBack()}")
        if (navigator.currentDestination?.pane == ListDetailPaneScaffoldRole.Detail && currentChat == null && navigator.canNavigateBack()) {
            navigator.navigateBack()
        } else {
            navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, currentChat)
        }
    }
    CompositionLocalProvider(LocalHazeStyle provides HazeMaterials.thin()) {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                AnimatedPane {
                    val viewModel = koinViewModel<ChatListViewModel>()
                    val chats by viewModel.userChats.collectAsState()
                    ChatList(
                        chats = chats,
                        openChat = {
                            if (currentChat != it) {
                                selectChat(it)
                            } else {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, it)
                            }
                        },
                        deleteChat = viewModel::deleteChat,
                        navigationIcon = navigationIcon
                    )
                }
            },
            detailPane = {
                AnimatedPane {
                    val viewModel = koinViewModel<ChatDetailViewModel>()
                    val state by viewModel.state.collectAsState()
                    LaunchedEffect(navigator.currentDestination?.content) {
                        navigator.currentDestination?.content?.let { viewModel.loadChat(it) }
                    }
                    AnimatedContent(
                        targetState = navigator.currentDestination?.content != null,
                    ) { chatSelected ->
                        val navigateBackButton = @Composable {
                            IconButton(onClick = { navigator.navigateBack() }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(Res.string.back)
                                )
                            }
                        }
                        if (chatSelected) {
                            ChatDetail(
                                state = state,
                                sendMessage = viewModel::sendMessage,
                                toggleRecord = viewModel::toggleRecording,
                                sendImage = viewModel::sendImage,
                                playAudio = viewModel::playAudio,
                                stopAudio = viewModel::stopAudio,
                                navigationIcon = {
                                    Napier.d("ChatLayout: navigationIcon: canNavigateBack=${navigator.canNavigateBack()}")
                                    if (navigator.canNavigateBack()) {
                                        navigateBackButton()
                                    }
                                }
                            )
                        } else {
                            NewChat(
                                selectNewChat = selectChat,
                                navigationIcon = {
                                    if (navigator.canNavigateBack()) {
                                        navigateBackButton()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}
