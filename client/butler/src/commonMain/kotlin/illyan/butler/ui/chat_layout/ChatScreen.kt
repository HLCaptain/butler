package illyan.butler.ui.chat_layout

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.Navigator
import illyan.butler.ui.chat_detail.ChatDetailScreen
import illyan.butler.ui.chat_list.ChatListScreen
import illyan.butler.ui.components.ButlerListDetail
import illyan.butler.ui.components.FixedOffsetHorizontalTwoPaneStrategy
import illyan.butler.ui.components.FractionHorizontalTwoPaneStrategy

class ChatScreen : Screen {
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<ChatScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
        val containerSize = LocalWindowInfo.current.containerSize
        var selectedChat by rememberSaveable { mutableStateOf<String?>(null) }
        ButlerListDetail(
            strategy = when (containerSize.width) {
                in 0..599 -> FractionHorizontalTwoPaneStrategy(1f)
                in 600..1199 -> FractionHorizontalTwoPaneStrategy(0.4f)
                else -> FixedOffsetHorizontalTwoPaneStrategy(320.dp, true)
            },
            list = {
                Navigator(ChatListScreen { selectedChat = it })
            },
            detail = {
                AnimatedVisibility(visible = selectedChat != null) {
                    selectedChat?.let { ChatDetailScreen(it) }
                }
            }
        )
    }
}