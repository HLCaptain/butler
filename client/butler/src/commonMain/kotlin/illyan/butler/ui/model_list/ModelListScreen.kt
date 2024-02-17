package illyan.butler.ui.model_list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import illyan.butler.domain.model.DomainModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.select
import illyan.butler.ui.chat.ChatScreen
import illyan.butler.ui.components.MenuButton
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

class ModelListScreen : Screen {
    @Composable
    override fun Content() {
        val screenModel = getScreenModel<ModelListScreenModel>()
        val models by screenModel.availableModels.collectAsState()
        val newChatUUID by screenModel.newChatUUID.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        LaunchedEffect(newChatUUID) {
            if (newChatUUID != null) {
                navigator.pop()
                navigator.push(ChatScreen(newChatUUID!!))
                screenModel.onNavigateToChat()
            }
        }
        ModelList(
            models = models,
            selectModel = screenModel::startNewChat
        )
    }

    @Composable
    fun ModelList(
        models: List<DomainModel>,
        selectModel: (String) -> Unit
    ) {
        LazyColumn(
            modifier = Modifier.padding(8.dp)
        ) {
            items(models) {
                ModelListItem(
                    model = it,
                    selectModel = { selectModel(it.uuid) }
                )
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
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
                    text = model.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                MenuButton(
                    onClick = selectModel,
                    text = stringResource(Res.string.select)
                )
            }
        }
    }
}