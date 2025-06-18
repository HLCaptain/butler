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
import androidx.compose.material.icons.rounded.AttachMoney
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.movableContentOf
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
import androidx.compose.ui.focus.focusProperties
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
import illyan.butler.domain.model.FilterConfiguration
import illyan.butler.domain.model.FilterType
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.api
import illyan.butler.generated.resources.close
import illyan.butler.generated.resources.companies
import illyan.butler.generated.resources.device
import illyan.butler.generated.resources.filters
import illyan.butler.generated.resources.free
import illyan.butler.generated.resources.free_models_only
import illyan.butler.generated.resources.hosts
import illyan.butler.generated.resources.loading
import illyan.butler.generated.resources.model_id
import illyan.butler.generated.resources.new_chat
import illyan.butler.generated.resources.no_models_to_chat_with
import illyan.butler.generated.resources.search
import illyan.butler.generated.resources.select
import illyan.butler.generated.resources.server
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.FilterOption
import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.toPersistentMap
import kotlinx.collections.immutable.toPersistentSet
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun NewChat(
    selectModel: (AiSource) -> Unit,
    hazeState: HazeState = remember { HazeState() },
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val viewModel = koinViewModel<NewChatViewModel>()
    val state by viewModel.state.collectAsState()
    NewChat(
        state = state,
        selectModel = selectModel,
        onFilterChanged = viewModel::onFilterChanged,
        hazeState = hazeState,
        navigationIcon = navigationIcon,
    )
}

private fun filterModelsWithQuery(
    models: List<AiSource>?,
    query: String,
): List<AiSource>? {
    return if (query.isBlank()) models else models?.filter { model ->
        model.displayName.contains(query, ignoreCase = true) ||
                model.modelId.contains(query, ignoreCase = true)
    }
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalHazeApi::class, ExperimentalUuidApi::class
)
@Composable
fun NewChat(
    state: NewChatState,
    selectModel: (AiSource) -> Unit,
    onFilterChanged: (FilterConfiguration) -> Unit,
    hazeState: HazeState = remember { HazeState() },
    navigationIcon: @Composable (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var searchFilter by rememberSaveable { mutableStateOf("") }
    val regexFilters = state.filterConfiguration.filterOptions.mapNotNull { (filter, enabled) -> if (!enabled) null else filter as? FilterOption.RegexFilter }
    val filteredAiSources = remember(
        searchFilter,
        regexFilters,
        state.aiSources
    ) {
        val queries = regexFilters.map { it.pattern } + searchFilter
        queries.fold(state.aiSources) { models, pattern ->
            filterModelsWithQuery(models, pattern)
        }
    }
    var selectedModelId by remember { mutableStateOf<String?>(null) }
    val selectedModel = remember(selectedModelId, state.aiSources) {
        if (selectedModelId == null) null
        else state.aiSources?.firstOrNull { it.modelId == selectedModelId }
    }
    BackHandler(enabled = selectedModel != null) {
        selectedModelId = null
    }
    // FabState -> 0: close, 1: search, 2: filters
    var filtersFabOpen by rememberSaveable { mutableStateOf(false) }
    var filtersMenuShown by rememberSaveable { mutableStateOf(false) }
    var searchFiltersShown by rememberSaveable { mutableStateOf(false) }
    val isCompact = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT
    val filterByCompany = state.filterConfiguration.selectedFilterType == FilterType.COMPANIES
    SharedTransitionLayout {
        val blurRadius by animateFloatAsState(if (selectedModel != null) 32f else 0f)
        val darkenRatio by animateFloatAsState(if (selectedModel != null) 2.5f else 1f)
        val overlayAlpha by animateFloatAsState(if (selectedModel != null) 0.5f else 0f)
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
            }.then(if (selectedModel == null) Modifier else Modifier.focusProperties { canFocus = false })
        ) {
            Scaffold(
                modifier = Modifier.hazeEffect(hazeState).nestedScroll(scrollBehavior.nestedScrollConnection),
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
                containerColor = Color.Transparent,
                floatingActionButton = {
                    AnimatedContent(targetState = filtersFabOpen) { isFabOpen ->
                        Column(
                            modifier = Modifier.consumeWindowInsets(WindowInsets.systemBars),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            if (!isFabOpen) {
                                FiltersFAB(
                                    isCompact = isCompact,
                                    onFabShowChanged = { filtersFabOpen = it },
                                    animatedVisibilityScope = this@AnimatedContent,
                                    sharedTransitionScope = this@SharedTransitionLayout
                                )
                            } else {
                                FiltersOpenFAB(
                                    filtersMenuShown = filtersMenuShown,
                                    setFiltersMenuShown = { filtersMenuShown = it },
                                    isCompact = isCompact,
                                    filterByCompany = filterByCompany,
                                    filters = state.filterConfiguration.filterOptions.toPersistentMap(),
                                    setFilterEnabled = { filter, enabled ->
                                        onFilterChanged(
                                            state.filterConfiguration.copy(
                                                filterOptions = state.filterConfiguration.filterOptions + (filter to enabled),
                                            )
                                        )
                                    },
                                    setFilterByCompany = { onFilterChanged(state.filterConfiguration.copy(selectedFilterType = if (it) FilterType.COMPANIES else FilterType.MODEL_ID)) },
                                    hazeState = hazeState,
                                    onFabShowChanged = { filtersFabOpen = it },
                                    animatedVisibilityScope = this@AnimatedContent,
                                    sharedTransitionScope = this@SharedTransitionLayout
                                )
                            }
                            SearchOpenFAB(
                                searchFilter = searchFilter,
                                setSearchFilter = { searchFilter = it },
                                searchFiltersShown = searchFiltersShown,
                                setSearchFiltersShown = { searchFiltersShown = it },
                                isCompact = isCompact,
                                hazeState = hazeState,
                                selectedModel = selectedModel,
                                animatedVisibilityScope = this@AnimatedContent,
                                sharedTransitionScope = this@SharedTransitionLayout
                            )
                            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime))
                        }
                    }
                }
            ) { innerPadding ->
                ModelList(
                    modifier = Modifier.hazeSource(hazeState),
                    models = filteredAiSources,
                    filterByCompany = filterByCompany,
                    innerPadding = innerPadding,
                    focusModel = selectedModel,
                    onSelectModelId = { selectedModelId = it },
                    sharedTransitionScope = this@SharedTransitionLayout,
                )
            }
        }
        AnimatedContent(
            targetState = selectedModel,
        ) { model ->
            model?.let {
                val modelIdWithoutCompany = if (filterByCompany) model.modelId.substringAfter('/') else model.modelId
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
                        modelName = AiSource.getNameFromId(modelIdWithoutCompany),
                        modelId = model.modelId,
                        aiSourceSelection = filteredAiSources.orEmpty().filter { it.modelId == model.modelId }.toPersistentSet(),
                        selectModel = { aiSource -> selectModel(aiSource); selectedModelId = null },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun FiltersFAB(
    isCompact: Boolean,
    onFabShowChanged: (Boolean) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) = with(sharedTransitionScope) {
    val filtersIcon = movableContentOf {
        Icon(
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "filter_icon"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
            imageVector = Icons.Rounded.FilterList,
            contentDescription = stringResource(Res.string.filters)
        )
    }
    if (isCompact) {
        FloatingActionButton(
            modifier = Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState("filter_fab"),
                animatedVisibilityScope = animatedVisibilityScope,
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
            ),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = { onFabShowChanged(true) },
        ) {
            filtersIcon()
        }
    } else {
        ExtendedFloatingActionButton(
            modifier = Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState("search_fab"),
                animatedVisibilityScope = animatedVisibilityScope,
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
            ),
            onClick = { onFabShowChanged(true) },
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                filtersIcon()
                Text(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState("filters_text"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ).skipToLookaheadSize(),
                    text = stringResource(Res.string.filters)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class,
    ExperimentalUuidApi::class
)
@Composable
fun SearchOpenFAB(
    searchFilter: String,
    setSearchFilter: (String) -> Unit,
    searchFiltersShown: Boolean,
    setSearchFiltersShown: (Boolean) -> Unit,
    isCompact: Boolean,
    hazeState: HazeState,
    selectedModel: AiSource?,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) = with(sharedTransitionScope) {
    val focusRequester = remember { FocusRequester() }
    ExposedDropdownMenuBox(
        expanded = searchFiltersShown,
        onExpandedChange = setSearchFiltersShown
    ) {
        Row(
            modifier = Modifier
                .padding(start = 24.dp)
                .then(if (isCompact) Modifier.fillMaxWidth() else Modifier.widthIn(max = 320.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ButlerTextField(
                modifier = Modifier
                    .weight(1f)
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState("search_filter"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    )
                    .clip(ButlerTextFieldDefaults.Shape)
                    .hazeEffect(hazeState)
                    .focusRequester(focusRequester),
                value = searchFilter,
                onValueChange = setSearchFilter,
                leadingIcon = {
                    Icon(
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "search_icon"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                        imageVector = Icons.Rounded.Search,
                        contentDescription = stringResource(Res.string.search)
                    )
                }
            )
        }
        LaunchedEffect(selectedModel) {
            if (selectedModel == null) {
                focusRequester.requestFocus()
            } else {
                focusRequester.freeFocus()
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FiltersOpenFAB(
    filtersMenuShown: Boolean,
    setFiltersMenuShown: (Boolean) -> Unit,
    isCompact: Boolean,
    filterByCompany: Boolean,
    setFilterByCompany: (Boolean) -> Unit,
    filters: PersistentMap<FilterOption, Boolean>,
    setFilterEnabled: (FilterOption, Boolean) -> Unit,
    hazeState: HazeState,
    onFabShowChanged: (Boolean) -> Unit,
    animatedVisibilityScope: AnimatedVisibilityScope,
    sharedTransitionScope: SharedTransitionScope
) = with(sharedTransitionScope) {
    ExposedDropdownMenuBox(
        expanded = filtersMenuShown,
        onExpandedChange = setFiltersMenuShown
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
                            animatedVisibilityScope = animatedVisibilityScope,
                            resizeMode = SharedTransitionScope.ResizeMode.ScaleToBounds()
                        ),
                    checked = filtersMenuShown,
                    onCheckedChange = setFiltersMenuShown,
                    border = BorderStroke(width = if (filtersMenuShown) 2.dp else 0.dp, color = MaterialTheme.colorScheme.secondary),
                    colors = IconButtonDefaults.iconToggleButtonColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        checkedContentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        checkedContainerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = stringResource(Res.string.filters)
                    )
                }

                FilledIconButton(
                    onClick = { onFabShowChanged(false) },
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
                modifier = Modifier.then(if (isCompact) Modifier.fillMaxWidth() else Modifier.widthIn(max = 320.dp)),
                hazeState = hazeState,
                selectedIndex = if (filterByCompany) 0 else 1,
                tabLabels = listOf(
                    stringResource(Res.string.companies),
                    stringResource(Res.string.model_id)
                ),
                onIndexChanged = { index ->
                    setFilterByCompany(index == 0)
                }
            )
        }
        ButlerDropdownMenu(
            expanded = filtersMenuShown,
            onDismissRequest = { setFiltersMenuShown(false) },
            matchTextFieldWidth = false
        ) {
            ButlerDropdownMenuDefaults.DropdownMenuItem {
                Text(
                    text = stringResource(Res.string.filters),
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            filters.forEach { (filter, enabled) ->
                ButlerDropdownMenuDefaults.DropdownMenuItem(
                    onClick = {
                        setFilterEnabled(filter, !enabled)
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = when (filter) {
                                FilterOption.FreeRegexFilter -> Icons.Rounded.AttachMoney
                                is FilterOption.RegexFilter -> Icons.Rounded.Search
                            },
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        ButlerCheckbox(
                            checked = enabled,
                            onCheckedChange = { enabled ->
                                setFilterEnabled(filter, enabled)
                            }
                        )
                    }
                ) {
                    Text(
                        text = when (filter) {
                            FilterOption.FreeRegexFilter -> stringResource(Res.string.free_models_only)
                            is FilterOption.RegexFilter -> "\"${filter.pattern}\""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
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
            .clip(CircleShape)
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
                        .clip(CircleShape)
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
                            .clip(CircleShape)
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
    models: List<AiSource>?,
    focusModel: AiSource?,
    filterByCompany: Boolean,
    onSelectModelId: (String?) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    innerPadding: PaddingValues = PaddingValues(0.dp),
) {
    val isLoading = remember(models) { models == null }
    val noAvailableModels = models?.isEmpty() == true && !isLoading
    val companyModels = remember(models) {
        // Remove the "$company/" prefix from the model IDs
        models
            ?.groupBy { it.modelId.substringBefore('/') }
            ?.mapValues { (company, models) ->
                models.map { model ->
                    model.modelId.removePrefix("$company/") to model
                }.distinctBy { it.second.modelId }.sortedBy { it.second.modelId }
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
                        modifier = Modifier.padding(horizontal = 16.dp).animateItem(),
                        text = company,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                items(items = models, key = { it.second.modelId }) { (modelIdWithoutCompany, model) ->
                    ModelListItemCompact(
                        onClick = { onSelectModelId(model.modelId) },
                        modifier = Modifier.fillMaxWidth().animateItem(),
                        modelName = AiSource.getNameFromId(modelIdWithoutCompany),
                        modelId = model.modelId,
                        sharedTransitionScope = sharedTransitionScope,
                        isSelected = focusModel == model,
                    )
                }
            }
        } else {
            items(items = (models ?: emptyList()), key = { it.modelId }) { model ->
                ModelListItemCompact(
                    onClick = { onSelectModelId(model.modelId) },
                    modifier = Modifier.fillMaxWidth().animateItem(),
                    modelName = model.displayName,
                    modelId = model.modelId,
                    sharedTransitionScope = sharedTransitionScope,
                    isSelected = focusModel == model,
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
            colors = ButlerCardDefaults.cardColors(
                containerColor = Color.Transparent
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "arrow$modelId"),
                        animatedVisibilityScope = this@AnimatedVisibility,
                    ).focusProperties { canFocus = false },
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
    aiSourceSelection: PersistentSet<AiSource>,
    selectModel: (AiSource) -> Unit,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    ButlerOutlinedCard(
        modifier = modifier.sharedBounds(
            rememberSharedContentState(key = "card$modelId"),
            animatedVisibilityScope = animatedVisibilityScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
        ),
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
                aiSourceSelection.forEach {
                    val provider = it.endpoint
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
                            ButlerTag {
                                Text(
                                    text = stringResource(
                                        when (it) {
                                            is AiSource.Api -> Res.string.api
                                            is AiSource.Server -> Res.string.server
                                            else -> Res.string.device
                                        }
                                    )
                                )
                            }
                        }
                        SmallMenuButton(
                            onClick = { selectModel(it) },
                            text = stringResource(Res.string.select)
                        )
                    }
                }
            }
        }
    }
}
