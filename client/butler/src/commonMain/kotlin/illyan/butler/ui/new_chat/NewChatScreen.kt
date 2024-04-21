package illyan.butler.ui.new_chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import illyan.butler.domain.model.DomainModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.select
import illyan.butler.ui.components.MenuButton
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class NewChatScreen(private val createdNewChat: (String) -> Unit) : Screen {
    @Composable
    override fun Content() {
        val screenModel = koinScreenModel<NewChatScreenModel>()
        val state by screenModel.state.collectAsState()
        // Make your Compose Multiplatform UI
        LaunchedEffect(state.newChatId) {
            if (state.newChatId != null) {
                createdNewChat(state.newChatId!!)
            }
        }

        ModelList(
            state = state,
            selectModel = screenModel::createChatWithModel
        )
    }
}

@Composable
fun ModelList(
    state: NewChatState,
    selectModel: (String) -> Unit
) {
    AnimatedVisibility(state.availableModels != null) {
        LazyColumn(
            modifier = Modifier.padding(8.dp).fillMaxHeight()
        ) {
            items(state.availableModels ?: emptyList()) {
                ModelListItem(
                    model = it,
                    selectModel = { selectModel(it.id) }
                )
            }
        }
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
fun ModelListItem(
    model: DomainModel,
    selectModel: () -> Unit
) {
    Card(
        onClick = selectModel
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = model.name ?: model.id,
                style = MaterialTheme.typography.headlineMedium
            )
            MenuButton(
                onClick = selectModel,
                text = stringResource(Res.string.select)
            )
        }
    }
}