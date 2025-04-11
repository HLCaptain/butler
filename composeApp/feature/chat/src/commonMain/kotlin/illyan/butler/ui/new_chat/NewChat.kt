package illyan.butler.ui.new_chat

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterList
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
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
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
import illyan.butler.generated.resources.companies
import illyan.butler.generated.resources.filters
import illyan.butler.generated.resources.filters_coming_soon
import illyan.butler.generated.resources.free
import illyan.butler.generated.resources.free_models_only
import illyan.butler.generated.resources.hosts
import illyan.butler.generated.resources.loading
import illyan.butler.generated.resources.model_id
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_models_to_chat_with
import illyan.butler.generated.resources.search
import illyan.butler.generated.resources.select
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
    ExperimentalHazeApi::class, ExperimentalHazeMaterialsApi::class
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
    var selectedModelId by rememberSaveable { mutableStateOf<String?>(null) }
    BackHandler(enabled = selectedModelId != null) {
        selectedModelId = null
    }
    // FabState -> 0: close, 1: search, 2: filters
    var fabState by rememberSaveable { mutableIntStateOf(0) }
    var filtersMenuShown by rememberSaveable { mutableStateOf(false) }
    var searchFiltersShown by rememberSaveable { mutableStateOf(false) }
    val isCompact = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    // TODO: refactor filters and save filters in local storage
    var filterByCompany by rememberSaveable { mutableStateOf(false) }
    val models = remember(
        serverModels,
        providerModels,
        localModels
    ) {
        if (serverModels != null || providerModels != null || localModels != null) {
            (serverModels.orEmpty() + providerModels.orEmpty() + localModels.orEmpty()).sortedBy { it.id }
        } else null
    }
    SharedTransitionLayout {
        val blurRadius by animateFloatAsState(if (selectedModelId != null) 32f else 0f)
        val darkenRatio by animateFloatAsState(if (selectedModelId != null) 2.5f else 1f)
        val overlayAlpha by animateFloatAsState(if (selectedModelId != null) 0.5f else 0f)
        val surfaceColor = MaterialTheme.colorScheme.surface
        Box(
            modifier = Modifier.graphicsLayer {
                renderEffect = BlurEffect(radiusX = blurRadius, radiusY = blurRadius)
            }.drawWithContent {
                drawContent()
                if (blurRadius > 0f) {
                    drawRect(
                        color = surfaceColor.darken(darkenRatio),
                        alpha = overlayAlpha
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
                    Column(Modifier.consumeWindowInsets(WindowInsets.systemBars)) {
                        AnimatedContent(
                            targetState = fabState
                        ) { fab ->
                            when (fab) {
                                0 -> {
                                    Column(horizontalAlignment = Alignment.End) {
                                        SmallFloatingActionButton(
                                            modifier = Modifier.sharedBounds(
                                                sharedContentState = rememberSharedContentState("filter_fab"),
                                                animatedVisibilityScope = this@AnimatedContent,
                                                resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                                            ),
                                            onClick = { fabState = 2 },
                                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.FilterList,
                                                contentDescription = stringResource(Res.string.filters)
                                            )
                                        }
                                        if (isCompact) {
                                            FloatingActionButton(
                                                modifier = Modifier.sharedBounds(
                                                    sharedContentState = rememberSharedContentState("search_fab"),
                                                    animatedVisibilityScope = this@AnimatedContent,
                                                    resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                                                ),
                                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                                onClick = { fabState = 1 },
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
                                                onClick = { fabState = 1 },
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
                                1 -> {
                                    val focusRequester = remember { FocusRequester() }

                                    ExposedDropdownMenuBox(
                                        expanded = searchFiltersShown,
                                        onExpandedChange = { searchFiltersShown = it }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(start = 24.dp)
                                                .then(if (isCompact) Modifier.fillMaxWidth() else Modifier.widthIn(max = 320.dp)),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            OutlinedIconToggleButton(
                                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                                checked = searchFiltersShown,
                                                onCheckedChange = { searchFiltersShown = it },
                                                border = BorderStroke(width = if (searchFiltersShown) 2.dp else 0.dp, color = MaterialTheme.colorScheme.primary),
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
                                                onClick = { fabState = 0 },
                                            ) {
                                                Icon(
                                                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                                    contentDescription = stringResource(Res.string.close)
                                                )
                                            }
                                        }
                                        ButlerDropdownMenu(
                                            expanded = searchFiltersShown,
                                            onDismissRequest = { searchFiltersShown = false },
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
                                        LaunchedEffect(selectedModelId) {
                                            if (selectedModelId == null) {
                                                focusRequester.requestFocus()
                                            } else {
                                                focusRequester.freeFocus()
                                            }
                                        }
                                    }
                                }
                                2 -> {
                                    ExposedDropdownMenuBox(
                                        expanded = filtersMenuShown,
                                        onExpandedChange = { filtersMenuShown = it }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(start = 24.dp),
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            Row {
                                                OutlinedIconToggleButton(
                                                    modifier = Modifier
                                                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                                        .sharedBounds(
                                                            sharedContentState = rememberSharedContentState("filter_fab"),
                                                            animatedVisibilityScope = this@AnimatedContent,
                                                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                                                        ),
                                                    checked = filtersMenuShown,
                                                    onCheckedChange = { filtersMenuShown = it },
                                                    border = BorderStroke(width = if (filtersMenuShown) 2.dp else 0.dp, color = MaterialTheme.colorScheme.secondary),
                                                    colors = IconButtonDefaults.iconToggleButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.surface,
                                                        contentColor = MaterialTheme.colorScheme.secondary
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Rounded.Tune,
                                                        contentDescription = stringResource(Res.string.filters)
                                                    )
                                                }

                                                FilledIconButton(
                                                    onClick = { fabState = 0 },
                                                    colors = IconButtonDefaults.filledIconButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                                    )
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                                        contentDescription = stringResource(Res.string.close)
                                                    )
                                                }
                                            }

                                            FiltersTab(
                                                modifier = Modifier,
                                                hazeState = hazeState,
                                                selectedIndex = if (filterByCompany) 0 else 1,
                                                tabLabels = listOf(
                                                    stringResource(Res.string.companies),
                                                    stringResource(Res.string.model_id)
                                                ),
                                                onIndexChanged = { index ->
                                                    filterByCompany = index == 0
                                                }
                                            )
                                        }
                                        ButlerDropdownMenu(
                                            expanded = filtersMenuShown,
                                            onDismissRequest = { filtersMenuShown = false },
                                            matchTextFieldWidth = false
                                        ) {
                                            Text(
                                                modifier = Modifier.padding(horizontal = 16.dp),
                                                text = stringResource(Res.string.filters_coming_soon)
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
                    filterByCompany = filterByCompany,
                    innerPadding = innerPadding,
                    focusModelId = selectedModelId,
                    onSelectModel = { selectedModelId = it },
                    sharedTransitionScope = this@SharedTransitionLayout,
                )
            }
        }
        AnimatedContent(
            targetState = selectedModelId,
        ) { id ->
            id?.let {
                val selectedModel = models?.firstOrNull { it.id == id }
                if (selectedModel != null) {
                    val modelIdWithoutCompany = if (filterByCompany) selectedModel.id.substringAfter('/') else id
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
                            modelName = selectedModel.copy(id = modelIdWithoutCompany).displayName,
                            modelId = selectedModel.id,
                            providers = providerModels?.filter { it.id == id }?.map { it.endpoint } ?: emptyList(),
                            server = serverModels?.filter { it.id == id }?.map { it.endpoint } ?: emptyList(),
                            selectServerModel = { modelId, provider -> state.userId?.let { selectModel(modelId, provider, it) } },
                            selectProviderModel = { modelId, provider -> state.clientId?.let { selectModel(modelId, provider, it) } },
                            selectLocalModel = { modelId -> state.clientId?.let { selectModel(modelId, "", it) } }, // TODO: support local models properly
                        )
                    }
                } else {
                    LaunchedEffect(Unit) {
                        selectedModelId = null
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalHazeMaterialsApi::class)
@Composable
fun FiltersTab(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    selectedIndex: Int,
    tabLabels: List<String>,
    onIndexChanged: (Int) -> Unit,
) {
    var tabRowHeightPx by remember { mutableIntStateOf(0) }
    val indicatorPadding = 6.dp
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100))
            .hazeEffect(hazeState, HazeMaterials.ultraThin())
            .border(
                1.dp,
                MaterialTheme.colorScheme.surfaceVariant,
                CircleShape
            )
    ) {
        val interactionSources = remember(tabLabels.size) { tabLabels.map { _ -> MutableInteractionSource() } }
        SecondaryTabRow(
            modifier = Modifier.onSizeChanged { tabRowHeightPx = it.height },
            selectedTabIndex = selectedIndex,
            indicator = {
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier
                        .tabIndicatorOffset(selectedIndex, matchContentSize = false)
                        .padding(indicatorPadding)
                        .clip(RoundedCornerShape(100))
                        .hazeEffect(hazeState, HazeMaterials.thin(MaterialTheme.colorScheme.primaryContainer)),
                    height = with(LocalDensity.current) { tabRowHeightPx.toDp() - indicatorPadding },
                    color = Color.Transparent
                )
            },
            divider = {},
            containerColor = Color.Transparent,
            contentColor = Color.Transparent
        ) {
            tabLabels.indices.forEach { index ->
                Tab(
                    modifier = Modifier
                        .height(56.dp)
                        .indication(
                            interactionSource = interactionSources[index],
                            indication = null // Don't show ripple effect on FULL tab
                        ),
                    selected = selectedIndex == index,
                    onClick = {},
                    enabled = false // Don't show ripple effect on FULL tab
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(indicatorPadding)
                            .clip(RoundedCornerShape(100))
                            .indication(
                                interactionSource = interactionSources[index],
                                indication = LocalIndication.current
                            )
                    )
                }
            }
        }
        SecondaryTabRow(
            selectedTabIndex = selectedIndex,
            indicator = {},
            divider = {},
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.secondary,
        ) {
            tabLabels.forEachIndexed { index, text ->
                Tab(
                    modifier = Modifier
                        .height(56.dp)
                        .indication(
                            interactionSource = interactionSources[index],
                            indication = null // Don't show ripple effect on FULL tab
                        ),
                    selected = selectedIndex == index,
                    onClick = {},
                    enabled = false,
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable(
                                interactionSource = interactionSources[index],
                                indication = null,
                                onClick = { onIndexChanged(index) }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            modifier = Modifier.padding(vertical = 6.dp),
                            text = text.uppercase(),
                            fontWeight = FontWeight.Bold
                        )
                    }
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
    filterByCompany: Boolean,
    onSelectModel: (String?) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    innerPadding: PaddingValues = PaddingValues(0.dp),
) {
    val isLoading = remember(models) { models == null }
    val noAvailableModels = models?.isEmpty() == true && !isLoading
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
        modifier = modifier.fillMaxHeight().consumeWindowInsets(innerPadding).imePadding(),
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
        if (filterByCompany) {
            companyModels.forEach { (company, models) ->
                item {
                    Text(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        text = company,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                items(items = models, key = { it.second.id }) { (modelIdWithoutCompany, model) ->
                    ModelListItemCompact(
                        onClick = { onSelectModel(model.id) },
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        modelName = model.copy(id = modelIdWithoutCompany).displayName,
                        modelId = model.id,
                        sharedTransitionScope = sharedTransitionScope,
                        isSelected = focusModelId == model.id,
                    )
                }
            }
        } else {
            items(items = (models ?: emptyList()), key = { it.id }) { model ->
                ModelListItemCompact(
                    onClick = { onSelectModel(model.id) },
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    modelName = model.displayName,
                    modelId = model.id,
                    sharedTransitionScope = sharedTransitionScope,
                    isSelected = focusModelId == model.id,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModelListItemCompact(
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope,
    isSelected: Boolean,
    modelName: String,
    modelId: String,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    AnimatedVisibility(
        visible = !isSelected,
        modifier = modifier
    ) {
        ButlerCard(
            modifier = Modifier.sharedBounds(
                rememberSharedContentState(key = "card$modelId"),
                animatedVisibilityScope = this,
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
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
                        animatedVisibilityScope = this@AnimatedVisibility,
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.weight(1f, fill = false).clip(RoundedCornerShape(6.dp))) {
                            Text(
                                modifier = tooltipModifier
                                    .sharedElement(
                                        rememberSharedContentState(key = "title$modelId"),
                                        animatedVisibilityScope = this@AnimatedVisibility,
                                    ).skipToLookaheadSize()
                                    .padding(horizontal = 4.dp, vertical = 2.dp),
                                text = modelName.replace(":free", ""),
                                maxLines = 1,
                                style = MaterialTheme.typography.titleLarge,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (modelName.contains("free")) {
                            ButlerTag(
                                modifier = Modifier.sharedElement(
                                    rememberSharedContentState(key = "free_tag$modelId"),
                                    animatedVisibilityScope = this@AnimatedVisibility,
//                                    renderInOverlayDuringTransition = false
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.weight(1f, fill = false).clip(RoundedCornerShape(6.dp))) {
                                Text(
                                    modifier = tooltipModifier
                                        .sharedElement(
                                            rememberSharedContentState(key = "title$modelId"),
                                            animatedVisibilityScope = animatedVisibilityScope,
                                        ).skipToLookaheadSize()
                                        .padding(horizontal = 4.dp, vertical = 2.dp),
                                    text = modelName.replace(":free", ""),
                                    maxLines = 2,
                                    style = MaterialTheme.typography.titleLarge,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                            if (modelName.contains("free")) {
                                ButlerTag(
                                    modifier = Modifier.sharedElement(
                                        rememberSharedContentState(key = "free_tag$modelId"),
                                        animatedVisibilityScope = animatedVisibilityScope,
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
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = stringResource(Res.string.model_id),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = modelId,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(Res.string.hosts),
                    style = MaterialTheme.typography.titleMedium
                )
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
                            text = stringResource(Res.string.select)
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
