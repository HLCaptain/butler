package illyan.butler.ui.new_chat

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.ExpandableCard
import illyan.butler.core.ui.components.MenuButton
import illyan.butler.domain.model.DomainModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.loading
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.select_self_hosted
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun NewChat(
    selectNewChat: (String) -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val viewModel = koinViewModel<NewChatViewModel>()
    val state by viewModel.state.collectAsState()
    DisposableEffect(state) {
        state.newChatId?.let { selectNewChat(it) }
        onDispose { viewModel.clearNewChatId() }
    }
    NewChat(
        state = state,
        selectModel = viewModel::createChatWithModel,
        navigationIcon = navigationIcon,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewChat(
    state: NewChatState,
    selectModel: (String, String?) -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(Res.string.new_chat),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = navigationIcon ?: {},
            )
        },
    ) { innerPadding ->
        Crossfade(
            targetState = state.availableModels
        ) { models ->
            if (models == null) {
                Text(
                    text = stringResource(Res.string.loading),
                    style = MaterialTheme.typography.headlineSmall,
                )
            } else if (models.isNotEmpty()) {
                ModelList(
                    state = state,
                    selectModel = selectModel,
                    innerPadding = innerPadding
                )
            } else {
                Text("No models available")
            }
        }
    }
}

@Composable
fun ModelList(
    modifier: Modifier = Modifier,
    state: NewChatState,
    selectModel: (String, String?) -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    LazyColumn(
        modifier = modifier.fillMaxHeight().padding(top = innerPadding.calculateTopPadding()),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(state.availableModels?.toList() ?: emptyList()) {
            ModelListItem(
                model = it.first,
                providers = it.second,
                selectModelWithProvider = { provider -> selectModel(it.first.id, provider) }
            )
        }
    }
}

@Composable
fun ModelListItem(
    model: DomainModel,
    providers: List<String>,
    selectModelWithProvider: (String?) -> Unit,
    isSelfHostAvailable: Boolean = true
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    ExpandableCard(
        onClick = { isExpanded = !isExpanded },
        isExpanded = isExpanded,
        expandedContent = {
            Column(
                modifier = Modifier.padding(start = 56.dp)
            ) {
                providers.forEach { provider ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = provider,
                            style = MaterialTheme.typography.bodyMedium,
                            overflow = TextOverflow.Ellipsis,
                        )
                        MenuButton(
                            onClick = { selectModelWithProvider(provider) },
                            text = stringResource(Res.string.select_host)
                        )
                    }
                }
            }
        }
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { isExpanded = !isExpanded }) {
                    if (isExpanded) {
                        Icon(
                            imageVector = Icons.Rounded.ExpandLess,
                            contentDescription = "Collapse"
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Rounded.ExpandMore,
                            contentDescription = "Expand"
                        )
                    }
                }
                Text(
                    text = model.name ?: model.id,
                    maxLines = 1,
                    style = MaterialTheme.typography.headlineMedium,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            MenuButton(
                onClick = { selectModelWithProvider(null) },
                text = stringResource(Res.string.select_self_hosted),
                enabled = isSelfHostAvailable
            )
        }
    }
}