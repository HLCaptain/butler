package illyan.butler.ui.new_chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.window.core.layout.WindowWidthSizeClass
import com.materialkolor.ktx.darken
import dev.chrisbanes.haze.ExperimentalHazeApi
import dev.chrisbanes.haze.HazeInputScale
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerCheckbox
import illyan.butler.core.ui.components.ButlerDropdownMenu
import illyan.butler.core.ui.components.ButlerDropdownMenuDefaults
import illyan.butler.core.ui.components.ButlerOutlinedCard
import illyan.butler.core.ui.components.ButlerTag
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.components.ButlerTextFieldDefaults
import illyan.butler.core.ui.components.MediumCircularProgressIndicator
import illyan.butler.core.ui.components.PlainTooltipWithContent
import illyan.butler.core.ui.components.SmallMenuButton
import illyan.butler.core.ui.components.mediumDialogWidth
import illyan.butler.core.ui.utils.BackHandler
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
    SharedTransitionLayout {
        var selectedModelId by rememberSaveable { mutableStateOf<String?>(null) }
        BackHandler(enabled = selectedModelId != null) {
            selectedModelId = null
        }
        AnimatedContent(
            targetState = selectedModelId,
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { id ->
            val blurRadius by animateFloatAsState(if (id != null) 32f else 0f)
            val darkenRatio by animateFloatAsState(if (id != null) 2.5f else 1f)
            val surfaceColor = MaterialTheme.colorScheme.surface
            val models = remember(
                serverModels,
                providerModels,
                localModels
            ) {
                if (serverModels != null || providerModels != null || localModels != null) {
                    serverModels.orEmpty() + providerModels.orEmpty() + localModels.orEmpty()
                } else null
            }
            Box(
                modifier = Modifier.graphicsLayer {
                    renderEffect = BlurEffect(radiusX = blurRadius, radiusY = blurRadius)
                }.drawWithContent {
                    drawContent()
                    if (blurRadius > 0f) {
                        drawRect(
                            color = surfaceColor.darken(darkenRatio),
                            alpha = 0.5f
                        )
                    }
                }
            ) {
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
                                                FilledIconButton(
                                                    onClick = { isTextFieldFocused = false },
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                                        contentDescription = stringResource(Res.string.close)
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
                        models = models,
                        innerPadding = innerPadding,
                        focusModelId = selectedModelId,
                        onSelectModel = { selectedModelId = it },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent
                    )
                }
            }
            selectedModelId?.let { modelId ->
                val modelIdWithoutCompany = modelId.substringAfter('/')
                Box(
                    modifier = Modifier.zIndex(1f).fillMaxSize().clickable(
                        enabled = true,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { selectedModelId = null },
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    ModelListItemExpanded(
                        modifier = Modifier.mediumDialogWidth(),
                        onClick = { selectedModelId = null },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animatedVisibilityScope = this@AnimatedContent,
                        modelName = (models ?: emptyList()).first { it.id == modelId }.copy(id = modelIdWithoutCompany).displayName,
                        modelId = (models ?: emptyList()).first { it.id == modelId }.id,
                        providers = providerModels?.filter { it.id == modelId }?.map { it.endpoint } ?: emptyList(),
                        server = serverModels?.filter { it.id == modelId }?.map { it.endpoint } ?: emptyList(),
                        selectServerModel = { modelId, provider -> state.userId?.let { selectModel(modelId, provider, it) } },
                        selectProviderModel = { modelId, provider -> state.clientId?.let { selectModel(modelId, provider, it) } },
                        selectLocalModel = { modelId -> state.clientId?.let { selectModel(modelId, "", it) } }, // TODO: support local models properly
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModelList(
    modifier: Modifier = Modifier,
    models: List<DomainModel>?,
    focusModelId: String?,
    onSelectModel: (String?) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    innerPadding: PaddingValues = PaddingValues(0.dp),
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Column(modifier = modifier) {
        val isLoading = remember(models) { models == null }
        val noAvailableModels = models?.isEmpty() == true && !isLoading
        val companyCategoriesEnabled = remember(models) { (models.orEmpty()).filter { it.id.contains('/') }.size > (models?.size ?: 0) / 2 + 10 }
        val companyModels = remember(models) {
            // Remove the "$company/" prefix from the model IDs
            models
                ?.groupBy { it.id.substringBefore('/') }
                ?.mapValues { (company, models) ->
                    models.map { model ->
                        model.id.removePrefix("$company/") to model
                    }.distinctBy { it.second.id }.sortedBy { it.second.id }
                }?.mapKeys { (company, _) ->
                    company.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }?.toList()?.sortedBy { it.first } ?: emptyList()
        }

        LazyColumn(
            modifier = Modifier.fillMaxHeight().consumeWindowInsets(innerPadding).imePadding(),
            contentPadding = PaddingValues(12.dp) + innerPadding + PaddingValues(bottom = 56.dp + 16.dp), // Base + inner + FAB height + FAB spacing
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (isLoading) {
                item("is_loading") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(innerPadding + PaddingValues(16.dp))
                            .consumeWindowInsets(innerPadding)
                            .animateItem(),
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
            }
            if (noAvailableModels) {
                item("no_models") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(innerPadding + PaddingValues(16.dp))
                            .consumeWindowInsets(innerPadding)
                            .animateItem(),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(Res.string.no_models_to_chat_with),
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                }
            }
            if (companyCategoriesEnabled) {
                companyModels.forEach { (company, models) ->
                    item {
                        Text(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = company,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    items(items = models.filterNot { it.second.id == focusModelId }, key = { it.second.id }) { (modelIdWithoutCompany, model) ->
                        ModelListItemCompact(
                            onClick = { onSelectModel(model.id) },
                            modifier = Modifier.fillMaxWidth().animateItem(),
                            modelName = model.copy(id = modelIdWithoutCompany).displayName,
                            modelId = model.id,
                            sharedTransitionScope = sharedTransitionScope,
                            animatedVisibilityScope = animatedVisibilityScope,
                        )
                    }
                }
            } else {
                items(items = (models ?: emptyList()).filterNot { it.id == focusModelId }, key = { it.id }) { model ->
                    ModelListItemCompact(
                        onClick = { onSelectModel(model.id) },
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        modelName = model.displayName,
                        modelId = model.id,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModelListItemCompact(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modelName: String,
    modelId: String,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    ButlerCard(
        modifier = modifier.sharedBounds(
            rememberSharedContentState(key = "card$modelId"),
            animatedVisibilityScope = animatedVisibilityScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            renderInOverlayDuringTransition = false
        ),
        onClick = onClick,
        contentPadding = ButlerCardDefaults.CompactContentPadding,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "arrow$modelId"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    renderInOverlayDuringTransition = false
                ),
                onClick = onClick
            ) {
                Icon(
                    imageVector = Icons.Rounded.ExpandMore,
                    contentDescription = "Expand"
                )
            }
            PlainTooltipWithContent(
                modifier = Modifier.weight(1f, fill = false),
                onClick = onClick,
                tooltip = { Text(modelId) }
            ) { tooltipModifier ->
                Row(
                    modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = tooltipModifier
                            .sharedElement(
                                rememberSharedContentState(key = "title$modelId"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                renderInOverlayDuringTransition = false
                            )
                            .weight(1f, fill = false)
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        text = modelName.replace(":free", ""),
                        maxLines = 1,
                        style = MaterialTheme.typography.titleLarge,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (modelName.contains("free")) {
                        ButlerTag(
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "free_tag$modelId"),
                                animatedVisibilityScope = animatedVisibilityScope,
                                renderInOverlayDuringTransition = false
                            )
                        ) {
                            Text(text = stringResource(Res.string.free))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModelListItemExpanded(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modelName: String,
    modelId: String,
    providers: List<String>,
    server: List<String>,
    selectServerModel: (String, String) -> Unit,
    selectProviderModel: (String, String) -> Unit,
    selectLocalModel: (String) -> Unit,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    ButlerOutlinedCard(
        modifier = modifier.sharedBounds(
            rememberSharedContentState(key = "card$modelId"),
            animatedVisibilityScope = animatedVisibilityScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            renderInOverlayDuringTransition = false
        ),
        onClick = onClick,
        contentPadding = ButlerCardDefaults.CompactContentPadding,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "arrow$modelId"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            renderInOverlayDuringTransition = false
                        ),
                        onClick = onClick
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ExpandLess,
                            contentDescription = "Collapse"
                        )
                    }
                    PlainTooltipWithContent(
                        modifier = Modifier.weight(1f, fill = false),
                        onClick = onClick,
                        tooltip = { Text(modelId) }
                    ) { tooltipModifier ->
                        Row(
                            modifier = Modifier.clip(RoundedCornerShape(6.dp)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = tooltipModifier
                                    .sharedElement(
                                        rememberSharedContentState(key = "title$modelId"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        renderInOverlayDuringTransition = false,
                                    )
                                    .weight(1f, fill = false)
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                text = modelName.replace(":free", ""),
                                maxLines = 1,
                                style = MaterialTheme.typography.titleLarge,
                                overflow = TextOverflow.Ellipsis,
                            )
                            if (modelName.contains("free")) {
                                ButlerTag(
                                    modifier = Modifier.sharedElement(
                                        rememberSharedContentState(key = "free_tag$modelId"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        renderInOverlayDuringTransition = false
                                    )
                                ) {
                                    Text(text = stringResource(Res.string.free))
                                }
                            }
                        }
                    }
                }
            }
            Column(
                modifier = Modifier.padding(start = 16.dp)
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
                                modifier = Modifier.weight(1f, fill = false).skipToLookaheadSize(),
                                text = provider,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            AnimatedVisibility(visible = provider in providers) {
                                ButlerTag { Text(text = stringResource(Res.string.api)) }
                            }
                        }
                        SmallMenuButton(
                            onClick = { selectProviderModel(modelId, provider) },
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
                                modifier = Modifier.weight(1f, fill = false).skipToLookaheadSize(),
                                text = provider,
                                style = MaterialTheme.typography.bodyMedium,
                                overflow = TextOverflow.Ellipsis,
                            )
                            AnimatedVisibility(visible = provider in server) {
                                ButlerTag { Text(text = stringResource(Res.string.server)) }
                            }
                        }
                        SmallMenuButton(
                            onClick = { selectServerModel(modelId, provider) },
                            text = stringResource(Res.string.select_host)
                        )
                    }
                }
            }
        }
    }
}
