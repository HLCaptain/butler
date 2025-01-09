package illyan.butler.ui.new_chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerExpandableCard
import illyan.butler.core.ui.components.ButlerTag
import illyan.butler.core.ui.components.MenuButton
import illyan.butler.core.ui.utils.plus
import illyan.butler.domain.model.DomainModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.api
import illyan.butler.generated.resources.loading
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.select_host
import illyan.butler.generated.resources.self_hosted
import illyan.butler.generated.resources.server
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
        onDispose {
            state.newChatId?.let { viewModel.clearNewChatId() }
        }
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
    selectModel: (String, String, String) -> Unit,
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
        ModelList(
            serverModels = state.serverModels,
            providerModels = state.providerModels,
            localModels = state.localModels,
            selectServerModel = { modelId, provider -> state.userId?.let { selectModel(modelId, provider, it) } },
            selectProviderModel = { modelId, provider -> state.clientId?.let { selectModel(modelId, provider, it) } },
            selectLocalModel = { modelId -> state.clientId?.let { selectModel(modelId, "", it) } }, // TODO: support local models properly
            innerPadding = innerPadding
        )
    }
}

@Composable
fun ModelList(
    modifier: Modifier = Modifier,
    serverModels: List<DomainModel>?,
    providerModels: List<DomainModel>?,
    localModels: List<DomainModel>?,
    selectServerModel: (String, String) -> Unit,
    selectProviderModel: (String, String) -> Unit,
    selectLocalModel: (String) -> Unit,
    innerPadding: PaddingValues = PaddingValues(0.dp)
) {
    Crossfade(
        targetState = Triple(serverModels, providerModels, localModels),
    ) { (server, provider, local) ->
        if (server == null && provider == null && local == null) {
            Text(
                text = stringResource(Res.string.loading),
                style = MaterialTheme.typography.headlineSmall,
            )
        } else if (!server.isNullOrEmpty() || !provider.isNullOrEmpty() || !local.isNullOrEmpty()) {
            val models = remember(server, provider, local) { server.orEmpty().plus(provider.orEmpty()).plus(local.orEmpty()) }
            LazyColumn(
                modifier = modifier.fillMaxHeight().consumeWindowInsets(innerPadding),
                contentPadding = PaddingValues(12.dp) + innerPadding,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                models.groupBy { it.id }.toList().forEach { (id, models) ->
                    items(models, key = { "${it.endpoint}/${it.id}" }) { model ->
                        ModelListItem(
                            modelName = model.displayName,
                            providers = models.mapNotNull { if (provider?.contains(it) == true) it.endpoint else null },
                            server = models.mapNotNull { if (server?.contains(it) == true) it.endpoint else null },
                            selectModelWithProvider = { provider -> selectProviderModel(id, provider) },
                            selectModelFromServerWithProvider = { provider -> selectServerModel(id, provider) },
                            selectLocalModel = { selectLocalModel(id) },
                            isSelfHostAvailable = local?.any { it.id == id } == true,
                        )
                    }
                }
            }
        } else {
            Text("No models available")
        }
    }
}

@Composable
fun ModelListItem(
    modelName: String,
    providers: List<String>,
    server: List<String>,
    selectModelWithProvider: (String) -> Unit,
    selectModelFromServerWithProvider: (String) -> Unit,
    selectLocalModel: () -> Unit,
    isSelfHostAvailable: Boolean = false
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    ButlerExpandableCard(
        onClick = { isExpanded = !isExpanded },
        isExpanded = isExpanded,
        contentPadding = ButlerCardDefaults.CompactContentPadding,
        expandedContent = {
            Column(
                modifier = Modifier.padding(start = 56.dp)
            ) {
                providers.forEach { provider ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                modifier = Modifier.weight(1f, fill = false),
                                text = provider,
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis,
                            )
                            AnimatedVisibility(visible = provider in providers) {
                                ButlerTag { Text(text = stringResource(Res.string.api)) }
                            }
                        }
                        MenuButton(
                            onClick = { selectModelWithProvider(provider) },
                            text = stringResource(Res.string.select_host)
                        )
                    }
                }
                server.forEach { provider ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                modifier = Modifier.weight(1f, fill = false),
                                text = provider,
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis,
                            )
                            AnimatedVisibility(visible = provider in server) {
                                ButlerTag { Text(text = stringResource(Res.string.server)) }
                            }
                        }
                        MenuButton(
                            onClick = { selectModelFromServerWithProvider(provider) },
                            text = stringResource(Res.string.select_host)
                        )
                    }
                }
            }
        }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                    modifier = Modifier.weight(1f),
                    text = modelName,
                    maxLines = 1,
                    style = MaterialTheme.typography.headlineMedium,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            MenuButton(
                onClick = selectLocalModel,
                text = stringResource(Res.string.self_hosted),
                enabled = isSelfHostAvailable
            )
        }
    }
}
