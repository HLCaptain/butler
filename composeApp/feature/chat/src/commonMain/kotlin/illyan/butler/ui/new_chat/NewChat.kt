package illyan.butler.ui.new_chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerCheckbox
import illyan.butler.core.ui.components.ButlerDropdownMenu
import illyan.butler.core.ui.components.ButlerDropdownMenuDefaults
import illyan.butler.core.ui.components.ButlerExpandableCard
import illyan.butler.core.ui.components.ButlerTag
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.components.ButlerTextFieldDefaults
import illyan.butler.core.ui.components.MediumCircularProgressIndicator
import illyan.butler.core.ui.components.MenuButton
import illyan.butler.core.ui.components.PlainTooltipWithContent
import illyan.butler.core.ui.utils.plus
import illyan.butler.domain.model.DomainModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.api
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.filters
import illyan.butler.generated.resources.free
import illyan.butler.generated.resources.free_models_only
import illyan.butler.generated.resources.loading
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_models_to_chat_with
import illyan.butler.generated.resources.search
import illyan.butler.generated.resources.select_host
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

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalHazeApi::class
)
@Composable
fun NewChat(
    state: NewChatState,
    selectModel: (String, String, String) -> Unit,
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var searchFilter by rememberSaveable { mutableStateOf("") }
    var freeFilterEnabled by rememberSaveable { mutableStateOf(false) }
    val serverModels = remember(searchFilter, freeFilterEnabled, state.serverModels) {
        val models = if (searchFilter.isBlank()) state.serverModels else
            state.serverModels?.filter {
                it.displayName.contains(
                    searchFilter,
                    ignoreCase = true
                ) || it.id.contains(searchFilter, ignoreCase = true)
            }
        if (freeFilterEnabled) models?.filter { it.displayName.contains("free") } else models
    }
    val providerModels = remember(searchFilter, freeFilterEnabled, state.providerModels) {
        val models = if (searchFilter.isBlank()) state.providerModels else
            state.providerModels?.filter {
                it.displayName.contains(
                    searchFilter,
                    ignoreCase = true
                ) || it.id.contains(searchFilter, ignoreCase = true)
            }
        if (freeFilterEnabled) models?.filter { it.displayName.contains("free") } else models
    }
    val localModels = remember(searchFilter, freeFilterEnabled, state.localModels) {
        val models = if (searchFilter.isBlank()) state.localModels else
            state.localModels?.filter {
                it.displayName.contains(
                    searchFilter,
                    ignoreCase = true
                ) || it.id.contains(searchFilter, ignoreCase = true)
            }
        if (freeFilterEnabled) models?.filter { it.displayName.contains("free") } else models
    }
    val hazeState = remember { HazeState() }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                modifier = Modifier.hazeEffect(hazeState) {
                    inputScale = HazeInputScale.None
                },
                title = {
                    Text(
                        stringResource(Res.string.new_chat),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = navigationIcon ?: {},
                colors = TopAppBarDefaults.topAppBarColors().copy(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent,
                ),
            )
        },
        floatingActionButton = {
            val isCompact = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
            var isTextFieldFocused by remember { mutableStateOf(false) }
            Column(Modifier.consumeWindowInsets(WindowInsets.systemBars)) {
                SharedTransitionLayout {
                    AnimatedContent(targetState = isTextFieldFocused to isCompact) { (focused, compact) ->
                        if (focused) {
                            val focusRequester = remember { FocusRequester() }
                            var filtersShown by rememberSaveable { mutableStateOf(false) }
                            ExposedDropdownMenuBox(
                                expanded = filtersShown,
                                onExpandedChange = { filtersShown = it }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(start = 24.dp)
                                        .then(if (compact) Modifier.fillMaxWidth() else Modifier.widthIn(max = 320.dp)),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    FilledIconButton(
                                        onClick = { isTextFieldFocused = false },
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                            contentDescription = stringResource(Res.string.close)
                                        )
                                    }
                                    ButlerTextField(
                                        modifier = Modifier
                                            .weight(1f)
                                            .sharedBounds(
                                                sharedContentState = rememberSharedContentState("search_filter"),
                                                animatedVisibilityScope = this@AnimatedContent,
                                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                            )
                                            .clip(ButlerTextFieldDefaults.Shape)
                                            .hazeEffect(hazeState)
                                            .focusRequester(focusRequester),
                                        value = searchFilter,
                                        onValueChange = { searchFilter = it },
                                        leadingIcon = {
                                            Icon(
                                                modifier = Modifier.sharedElement(
                                                    rememberSharedContentState(key = "search_icon"),
                                                    animatedVisibilityScope = this@AnimatedContent
                                                ),
                                                imageVector = Icons.Rounded.Search,
                                                contentDescription = stringResource(Res.string.search)
                                            )
                                        }
                                    )
                                    OutlinedIconToggleButton(
                                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                        checked = filtersShown,
                                        onCheckedChange = { filtersShown = it },
                                        border = BorderStroke(width = if (filtersShown) 2.dp else 0.dp, color = MaterialTheme.colorScheme.primary),
                                        colors = IconButtonDefaults.iconToggleButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surface,
                                            contentColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Tune,
                                            contentDescription = stringResource(Res.string.filters)
                                        )
                                    }
                                }
                                ButlerDropdownMenu(
                                    expanded = filtersShown,
                                    onDismissRequest = { filtersShown = false },
                                ) {
                                    ButlerDropdownMenuDefaults.DropdownMenuItem {
                                        Text(
                                            text = stringResource(Res.string.filters),
                                            style = MaterialTheme.typography.titleSmall,
                                        )
                                    }
                                    CompositionLocalProvider(
                                        LocalMinimumInteractiveComponentSize provides 40.dp
                                    ) {
                                        ButlerDropdownMenuDefaults.DropdownMenuItem(
                                            onClick = {
                                                freeFilterEnabled = !freeFilterEnabled
                                            },
                                            leadingIcon = {
                                                Text(text = "$")
                                            },
                                            trailingIcon = {
                                                ButlerCheckbox(
                                                    checked = freeFilterEnabled,
                                                    onCheckedChange = { freeFilterEnabled = it }
                                                )
                                            }
                                        ) {
                                            Text(
                                                text = stringResource(Res.string.free_models_only),
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                }
                                LaunchedEffect(Unit) {
                                    focusRequester.requestFocus()
                                }
                            }
                        } else if (compact) {
                            FloatingActionButton(
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState("search_fab"),
                                    animatedVisibilityScope = this@AnimatedContent,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                ),
                                onClick = { isTextFieldFocused = true },
                            ) {
                                Icon(
                                    modifier = Modifier.sharedElement(
                                        rememberSharedContentState(key = "search_icon"),
                                        animatedVisibilityScope = this@AnimatedContent
                                    ),
                                    imageVector = Icons.Rounded.Search,
                                    contentDescription = stringResource(Res.string.search)
                                )
                            }
                        } else {
                            ExtendedFloatingActionButton(
                                modifier = Modifier.sharedBounds(
                                    sharedContentState = rememberSharedContentState("search_fab"),
                                    animatedVisibilityScope = this@AnimatedContent,
                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                ),
                                onClick = { isTextFieldFocused = true },
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        modifier = Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState(key = "search_icon"),
                                            animatedVisibilityScope = this@AnimatedContent
                                        ),
                                        imageVector = Icons.Rounded.Search,
                                        contentDescription = stringResource(Res.string.search)
                                    )
                                    Text(
                                        modifier = Modifier.sharedElement(
                                            sharedContentState = rememberSharedContentState("search_filter"),
                                            animatedVisibilityScope = this@AnimatedContent
                                        ).skipToLookaheadSize(),
                                        text = stringResource(Res.string.search)
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
            }
        }
    ) { innerPadding ->
        ModelList(
            modifier = Modifier.hazeSource(hazeState),
            serverModels = serverModels,
            providerModels = providerModels,
            localModels = localModels,
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
    Column(modifier = modifier) {
        val models = remember(
            serverModels,
            providerModels,
            localModels
        ) { serverModels.orEmpty() + providerModels.orEmpty() + localModels.orEmpty() }
        val isLoading = remember(
            serverModels,
            providerModels,
            localModels
        ) { serverModels == null || providerModels == null || localModels == null }
        val noAvailableModels = models.isEmpty() && !isLoading
        AnimatedVisibility(visible = isLoading) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(innerPadding + PaddingValues(16.dp))
                    .consumeWindowInsets(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MediumCircularProgressIndicator()
                Text(
                    text = stringResource(Res.string.loading),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }

        AnimatedVisibility(visible = noAvailableModels) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(innerPadding + PaddingValues(16.dp))
                    .consumeWindowInsets(innerPadding),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.no_models_to_chat_with),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        }
        val companyCategoriesEnabled = remember(models) { models.filter { it.id.contains('/') }.size > models.size / 2 + 10 }
        val companyModels = remember(models) {
            // Remove the "$company/" prefix from the model IDs
            models
                .groupBy { it.id.substringBefore('/') }
                .mapValues { (company, models) ->
                    models.map { model ->
                        model.id.removePrefix("$company/") to model
                    }.distinctBy { it.second.id }.sortedBy { it.second.id }
                }.mapKeys { (company, _) ->
                    company.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }.toList().sortedBy { it.first }
        }
        LazyColumn(
            modifier = Modifier.fillMaxHeight().consumeWindowInsets(innerPadding).imePadding(),
            contentPadding = PaddingValues(12.dp) + innerPadding + PaddingValues(bottom = 56.dp + 16.dp), // Base + inner + FAB height + FAB spacing
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (companyCategoriesEnabled) {
                companyModels.forEach { (company, models) ->
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = company,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    items(items = models, key = { it.second.id }) { (modelIdWithoutCompany, model) ->
                        ModelListItem(
                            modifier = Modifier.animateItem(placementSpec = tween(0)),
                            modelName = model.copy(id = modelIdWithoutCompany).displayName,
                            modelId = model.id,
                            providers = providerModels?.filter { it.id == model.id }?.map { it.endpoint } ?: emptyList(),
                            server = serverModels?.filter { it.id == model.id }?.map { it.endpoint } ?: emptyList(),
                            selectModelWithProvider = { provider -> selectProviderModel(model.id, provider) },
                            selectModelFromServerWithProvider = { provider -> selectServerModel(model.id, provider) },
                            selectLocalModel = { selectLocalModel(model.id) }
                        )
                    }
                }
            } else {
                items(items = models, key = { it.id }) { model ->
                    ModelListItem(
                        modifier = Modifier.animateItem(placementSpec = tween(0)),
                        modelName = model.displayName,
                        modelId = model.id,
                        providers = providerModels?.filter { it.id == model.id }?.map { it.endpoint } ?: emptyList(),
                        server = serverModels?.filter { it.id == model.id }?.map { it.endpoint } ?: emptyList(),
                        selectModelWithProvider = { provider -> selectProviderModel(model.id, provider) },
                        selectModelFromServerWithProvider = { provider -> selectServerModel(model.id, provider) },
                        selectLocalModel = { selectLocalModel(model.id) },
                    )
                }
            }
        }
    }
}

@Composable
fun ModelListItem(
    modifier: Modifier = Modifier,
    modelName: String,
    modelId: String,
    providers: List<String>,
    server: List<String>,
    selectModelWithProvider: (String) -> Unit,
    selectModelFromServerWithProvider: (String) -> Unit,
    selectLocalModel: () -> Unit
) {
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    ButlerExpandableCard(
        modifier = modifier,
        onClick = { isExpanded = !isExpanded },
        isExpanded = isExpanded,
        contentPadding = ButlerCardDefaults.CompactContentPadding,
        expandedContent = {
            Column(
                modifier = Modifier.padding(start = 52.dp)
            ) {
                providers.forEach { provider ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                modifier = Modifier.weight(1f, fill = false),
                                text = provider,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
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
                PlainTooltipWithContent(
                    modifier = Modifier.weight(1f, fill = false),
                    onClick = { isExpanded = !isExpanded },
                    tooltip = { Text(modelId) }
                ) { tooltipModifier ->
                    Row(
                        modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = tooltipModifier
                                .weight(1f, fill = false)
                                .padding(horizontal = 4.dp, vertical = 2.dp),
                            text = modelName.replace(":free", ""),
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (modelName.contains("free")) {
                            ButlerTag {
                                Text(text = stringResource(Res.string.free))
                            }
                        }
                    }
                }
            }
        }
    }
}
