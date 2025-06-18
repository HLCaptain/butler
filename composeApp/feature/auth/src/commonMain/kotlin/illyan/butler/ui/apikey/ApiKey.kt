package illyan.butler.ui.apikey

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerDropdownMenu
import illyan.butler.core.ui.components.ButlerDropdownMenuDefaults
import illyan.butler.core.ui.components.ButlerElevatedCard
import illyan.butler.core.ui.components.ButlerMediumOutlinedButton
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerOutlinedCard
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.core.ui.components.PlainTooltipWithContent
import illyan.butler.core.ui.components.SmallCircularProgressIndicator
import illyan.butler.core.ui.components.mediumDialogWidth
import illyan.butler.domain.model.DomainModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.add_api_key
import illyan.butler.generated.resources.add_credentials
import illyan.butler.generated.resources.api_key
import illyan.butler.generated.resources.back
import illyan.butler.generated.resources.create
import illyan.butler.generated.resources.delete
import illyan.butler.generated.resources.edit
import illyan.butler.generated.resources.error
import illyan.butler.generated.resources.healthy
import illyan.butler.generated.resources.host
import illyan.butler.generated.resources.next
import illyan.butler.generated.resources.provider_url
import illyan.butler.generated.resources.required
import illyan.butler.generated.resources.save
import illyan.butler.generated.resources.test
import illyan.butler.generated.resources.unknown
import illyan.butler.shared.model.auth.ApiKeyCredential
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class ApiKeyCredentialEditItem(
    val name: String?,
    val providerUrl: String,
    val apiKey: String,
    val index: Int
)

@Serializable
data object NewApiKeyCredential

@Serializable
data object ApiKeyCredentialList

@Composable
fun ApiKey(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val viewModel = koinViewModel<ApiKeyViewModel>()
    val apiKeyCredentials by viewModel.apiKeyCredentials.collectAsState()
    val healthyCredentials by viewModel.healthyCredentials.collectAsState()
    val models by viewModel.modelsForCredential.collectAsState()
    ApiKeyScaffold(
        modifier = modifier,
        credentials = apiKeyCredentials,
        healthyCredentials = healthyCredentials,
        models = models.toMap(),
        createNewCredential = viewModel::addApiKeyCredential,
        testApiKeyCredential = viewModel::testEndpointForCredential,
        deleteCredential = viewModel::deleteApiKeyCredential,
        onBack = onBack,
        onNext = onNext
    )
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyScaffold(
    modifier: Modifier = Modifier,
    credentials: List<ApiKeyCredential>?,
    healthyCredentials: List<ApiKeyCredential>?,
    models: Map<ApiKeyCredential, List<DomainModel>>,
    createNewCredential: (ApiKeyCredential) -> Unit = {},
    testApiKeyCredential: (ApiKeyCredential) -> Unit = {},
    deleteCredential: (ApiKeyCredential) -> Unit = {},
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val animationTime = 500
    SharedTransitionLayout(
        modifier = Modifier
    ) {
        val navController = rememberNavController()
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = ApiKeyCredentialList,
            contentAlignment = Alignment.Center,
            sizeTransform = { SizeTransform(clip = true) },
            enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
            popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
            exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
            popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
        ) {
            composable<ApiKeyCredentialList> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(text = stringResource(Res.string.add_credentials)) },
                            navigationIcon = {
                                IconButton(onClick = onBack) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        AnimatedVisibility(
                            visible = credentials?.isNotEmpty() == true,
                            enter = expandVertically(expandFrom = Alignment.Bottom),
                            exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
                        ) {
                            ButlerMediumSolidButton(
                                onClick = onNext,
                                text = { Text(text = stringResource(Res.string.next)) },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = null
                                    )
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    ApiKeyCredentialList(
                        modifier = Modifier.padding(innerPadding),
                        credentials = credentials,
                        healthyCredentials = healthyCredentials,
                        createNewCredential = { navController.navigate(NewApiKeyCredential) },
                        editCredential = { index ->
                            credentials?.getOrNull(index)?.let {
                                navController.navigate(
                                    ApiKeyCredentialEditItem(
                                        // Host name based on providerUrl
                                        name = it.providerUrl.substringAfter("https://").substringBefore("/"),
                                        providerUrl = it.providerUrl,
                                        apiKey = it.apiKey,
                                        index = index
                                    )
                                )
                            }
                        },
                        deleteCredential = deleteCredential,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        animationScope = this@composable
                    )
                }
            }
            composable<ApiKeyCredentialEditItem> {
                val (item, index) = remember(credentials) {
                    val editItem = it.toRoute<ApiKeyCredentialEditItem>()
                    ApiKeyCredential(
                        providerUrl = editItem.providerUrl,
                        apiKey = editItem.apiKey
                    ) to editItem.index
                }
                EditApiKeyCredential(
                    modifier = Modifier.imePadding(),
                    item = item,
                    models = models[item] ?: emptyList(),
                    testCredential = {
                        testApiKeyCredential(it)
                    },
                    saveCredential = {
                        createNewCredential(it)
                        navController.navigateUp()
                    },
                    onBack = { navController.navigateUp() },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animationScope = this@composable,
                    key = index
                )
            }
            composable<NewApiKeyCredential> {
                var testCredential by rememberSaveable(
                    stateSaver = object : Saver<ApiKeyCredential?, String> {
                        override fun restore(value: String): ApiKeyCredential? {
                            return if (value.isBlank()) null else Json.decodeFromString(ApiKeyCredential.serializer(), value)
                        }

                        override fun SaverScope.save(value: ApiKeyCredential?): String {
                            return value?.let { Json.encodeToString(ApiKeyCredential.serializer(), it) } ?: ""
                        }
                    },
                ) { mutableStateOf(null) }
                NewApiKeyCredential(
                    modifier = Modifier.imePadding(),
                    models = models[testCredential] ?: emptyList(),
                    testCredential = {
                        testCredential = it
                        testApiKeyCredential(it)
                    },
                    saveCredential = {
                        createNewCredential(it)
                        navController.navigateUp()
                    },
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animationScope = this@composable,
                    onBack = { navController.navigateUp() }
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ApiKeyCredentialList(
    modifier: Modifier = Modifier,
    credentials: List<ApiKeyCredential>?,
    healthyCredentials: List<ApiKeyCredential>?,
    createNewCredential: () -> Unit,
    editCredential: (Int) -> Unit,
    deleteCredential: (ApiKeyCredential) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope
) {
    val lazyGridState = rememberLazyGridState()
    val minCellSize = 164.dp
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(minCellSize),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        state = lazyGridState
    ) {
        item("new_api_key") {
            NewApiKeyCredentialGridItem(
                modifier = Modifier.animateItem()
                    .sizeIn(minWidth = minCellSize, minHeight = minCellSize),
                createNewCredential = createNewCredential,
                sharedTransitionScope = sharedTransitionScope,
                animationScope = animationScope
            )
        }
        if (credentials.isNullOrEmpty()) {
            item(
                key = "no_api_keys",
                span = { GridItemSpan(currentLineSpan = this.maxCurrentLineSpan) }
            ) {
                NoApiKeyCredentialGridItem(
                    modifier = Modifier.animateItem()
                        .sizeIn(minWidth = minCellSize, minHeight = minCellSize),
                    lazyGridState = lazyGridState
                )
            }
        } else {
            itemsIndexed(
                credentials.distinctBy { it.providerUrl },
                key = { _, item -> item.providerUrl }) { index, item ->
                ApiKeyCredentialGridItem(
                    modifier = Modifier
                        .animateItem()
                        .sizeIn(minWidth = minCellSize, minHeight = minCellSize),
                    item = item,
                    key = index,
                    healthy = healthyCredentials?.contains(item),
                    editItem = { editCredential(index) },
                    deleteItem = { deleteCredential(item) },
                    sharedTransitionScope = sharedTransitionScope,
                    animationScope = animationScope
                )
            }
        }
    }
}

@Composable
fun NoApiKeyCredentialGridItem(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState
) {
    AnimatedContent(
        targetState = lazyGridState.layoutInfo.mainAxisItemSpacing > 1,
        transitionSpec = {
            val slideSpec = spring<IntOffset>(
                stiffness = Spring.StiffnessMediumLow,
                dampingRatio = (Spring.DampingRatioMediumBouncy + Spring.DampingRatioLowBouncy) / 2
            )
            if (targetState) {
                // Multiple spans in a row, arrow pointing to start
                slideIntoContainer(
                    animationSpec = slideSpec,
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                ) + fadeIn() togetherWith fadeOut(tween(0))
            } else {
                // Single row, arrow pointing up
                slideIntoContainer(
                    animationSpec = slideSpec,
                    towards = AnimatedContentTransitionScope.SlideDirection.Up
                ) + fadeIn() togetherWith fadeOut(tween(0))
            }
        }
    ) { multiSpan ->
        if (multiSpan) {
            Box(
                modifier = modifier.fillMaxHeight(),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(64.dp),
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(Res.string.create)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(64.dp),
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = null
                    )
                    Text(
                        text = stringResource(Res.string.add_credentials)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewApiKeyCredentialGridItem(
    modifier: Modifier = Modifier,
    createNewCredential: () -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope
) = with(sharedTransitionScope) {
    ButlerElevatedCard(
        modifier = Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState("new_api_key_bounds"),
            animatedVisibilityScope = animationScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
        ),
        onClick = createNewCredential,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = modifier.fillMaxSize().sharedBounds(
                sharedContentState = rememberSharedContentState("new_api_key_button_bounds"),
                animatedVisibilityScope = animationScope,
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
            ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    modifier = Modifier.size(64.dp).sharedElement(
                        sharedContentState = rememberSharedContentState("new_api_key_icon"),
                        animatedVisibilityScope = animationScope
                    ),
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState("new_api_key_text"),
                        animatedVisibilityScope = animationScope
                    ).skipToLookaheadSize(),
                    text = stringResource(Res.string.add_api_key),
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ApiKeyCredentialGridItem(
    modifier: Modifier = Modifier,
    item: ApiKeyCredential,
    key: Int,
    editItem: () -> Unit = {},
    deleteItem: () -> Unit = {},
    healthy: Boolean? = false,
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope
) = with(sharedTransitionScope) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    ButlerCard(
        modifier = Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState("api_key_bounds_$key"),
            animatedVisibilityScope = animationScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
        ),
        onClick = { showMenu = false; editItem() },
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(
                    start = 16.dp,
                    top = 16.dp,
                    bottom = 16.dp,
                    end = 32.dp
                ),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.host),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState("api_key_name_$key"),
                        animatedVisibilityScope = animationScope
                    ).skipToLookaheadSize(),
                    text = item.providerUrl.substringAfter("https://").substringBefore("/").takeIf { it.isNotBlank() }
                        ?: stringResource(Res.string.unknown),
                )
                Text(
                    text = stringResource(Res.string.provider_url),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    modifier = Modifier.sharedElement(
                        sharedContentState = rememberSharedContentState("api_key_provider_url_$key"),
                        animatedVisibilityScope = animationScope
                    ).skipToLookaheadSize(),
                    text = item.providerUrl.takeIf { it.isNotBlank() }
                        ?: stringResource(Res.string.unknown),
                )
            }
            ExposedDropdownMenuBox(
                modifier = Modifier.align(Alignment.TopEnd),
                expanded = showMenu,
                onExpandedChange = { showMenu = it }
            ) {
                IconToggleButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    checked = showMenu,
                    onCheckedChange = { showMenu = it },
                    colors = IconButtonDefaults.iconToggleButtonColors().copy(
                        checkedContentColor = MaterialTheme.colorScheme.primary,
                        contentColor = LocalContentColor.current
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = null
                    )
                }
                ButlerDropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    matchTextFieldWidth = false
                ) {
                    ButlerDropdownMenuDefaults.DropdownMenuItem(
                        onClick = { showMenu = false; editItem() },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null
                            )
                        },
                        content = { Text(stringResource(Res.string.edit)) }
                    )
                    ButlerDropdownMenuDefaults.DropdownMenuItem(
                        onClick = { showMenu = false; deleteItem() },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null
                            )
                        },
                        content = { Text(stringResource(Res.string.delete)) }
                    )
                }
            }

            Crossfade(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 8.dp, bottom = 8.dp)
                    .animateContentSize(),
                targetState = healthy
            ) { healthy ->
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (healthy == null) {
                        SmallCircularProgressIndicator()
                    } else {
                        Text(
                            text = stringResource(if (healthy) Res.string.healthy else Res.string.error),
                            style = MaterialTheme.typography.labelSmall
                        )
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = if (healthy) Icons.Rounded.Done else Icons.Rounded.Close,
                            contentDescription = null,
                            tint = if (healthy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EditApiKeyCredential(
    modifier: Modifier = Modifier,
    item: ApiKeyCredential,
    key: Int,
    models: List<DomainModel>,
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope,
    saveCredential: (ApiKeyCredential) -> Unit = {},
    testCredential: (ApiKeyCredential) -> Unit = {},
    onBack: () -> Unit
) = with(sharedTransitionScope) {
    var providerUrl by rememberSaveable(item.providerUrl) { mutableStateOf(item.providerUrl) }
    var apiKey by rememberSaveable(item.apiKey) { mutableStateOf(item.apiKey) }

    ButlerCard(
        modifier = modifier.sharedBounds(
            sharedContentState = rememberSharedContentState("api_key_bounds_$key"),
            animatedVisibilityScope = animationScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
        ),
        shape = RoundedCornerShape(0.dp),
        contentPadding = ButlerCardDefaults.CompactContentPadding
    ) {
        Box(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier.mediumDialogWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ApiKeyItemFields(
                    modifier = Modifier.fillMaxWidth(),
                    providerUrl = providerUrl,
                    apiKey = apiKey,
                    onProviderUrlChanged = { providerUrl = it },
                    onApiKeyChanged = { apiKey = it },
                    sharedTransitionScope = sharedTransitionScope,
                    animationScope = animationScope
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val testTitle = stringResource(Res.string.test)
                    ButlerMediumOutlinedButton(
                        modifier = Modifier.weight(testTitle.length.toFloat()),
                        onClick = {
                            testCredential(ApiKeyCredential(providerUrl, apiKey))
                        },
                        text = { Text(text = testTitle) },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState("new_api_key_test_icon"),
                                    animatedVisibilityScope = animationScope
                                ),
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null
                            )
                        }
                    )
                    val saveTitle = stringResource(Res.string.save)
                    ButlerMediumSolidButton(
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState("new_api_key_button_bounds"),
                            animatedVisibilityScope = animationScope,
                            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                        ).then(with(animationScope) {
                            Modifier.animateEnterExit(
                                enter = fadeIn(),
                                exit = fadeOut()
                            )
                        }).weight(saveTitle.length.toFloat()),
                        onClick = {
                            saveCredential(ApiKeyCredential(providerUrl, apiKey))
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState("new_api_key_icon"),
                                    animatedVisibilityScope = animationScope
                                ),
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState("new_api_key_text"),
                                    animatedVisibilityScope = animationScope
                                ).skipToLookaheadSize(),
                                text = saveTitle
                            )
                        }
                    )
                }
                val backTitle = stringResource(Res.string.back)
                ButlerMediumOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack,
                    text = { Text(text = backTitle) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                )

                AnimatedVisibility(visible = models.isNotEmpty()) {
                    ApiKeyTestModelListing(
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth(),
                        models = models
                    )
                }
            }
        }
    }
}

@Composable
fun ApiKeyTestModelListing(
    modifier: Modifier = Modifier,
    models: List<DomainModel>
) {
    ButlerOutlinedCard(
        modifier = modifier,
        contentPadding = PaddingValues(0.dp),
        colors = ButlerCardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(6.dp)
        )
    ) {
        LazyColumn(
            modifier = Modifier.heightIn(min = 24.dp * models.size),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(models, key = { it.id }) {
                PlainTooltipWithContent(
                    modifier = Modifier.animateItem(),
                    tooltip = { Text(text = it.toString()) }
                ) { tooltipModifier ->
                    Box(modifier = tooltipModifier) {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 2.dp),
                            text = it.displayName,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NewApiKeyCredential(
    modifier: Modifier = Modifier,
    models: List<DomainModel>,
    saveCredential: (ApiKeyCredential) -> Unit = {},
    testCredential: (ApiKeyCredential) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope,
    onBack: () -> Unit = {}
) = with(sharedTransitionScope) {
    var name by rememberSaveable { mutableStateOf("") }
    var providerUrl by rememberSaveable { mutableStateOf("") }
    var apiKey by rememberSaveable { mutableStateOf("") }
    var apiKeyFieldBlank by rememberSaveable { mutableStateOf(false) }
    var providerUrlFieldBlank by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(providerUrl) {
        if (providerUrlFieldBlank && providerUrl.isNotBlank()) {
            providerUrlFieldBlank = false
        }
    }

    LaunchedEffect(apiKey) {
        if (apiKeyFieldBlank && apiKey.isNotBlank()) {
            apiKeyFieldBlank = false
        }
    }

    ButlerCard(
        modifier = modifier.sharedBounds(
            sharedContentState = rememberSharedContentState("new_api_key_bounds"),
            animatedVisibilityScope = animationScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
        ),
        shape = RoundedCornerShape(0.dp),
        contentPadding = ButlerCardDefaults.CompactContentPadding
    ) {
        Box(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.mediumDialogWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ApiKeyItemFields(
                    modifier = Modifier.fillMaxWidth(),
                    providerUrl = providerUrl,
                    apiKey = apiKey,
                    showApiKeyError = apiKeyFieldBlank,
                    showProviderUrlError = providerUrlFieldBlank,
                    onProviderUrlChanged = { providerUrl = it },
                    onApiKeyChanged = { apiKey = it },
                    sharedTransitionScope = sharedTransitionScope,
                    animationScope = animationScope
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val testText = stringResource(Res.string.test)
                    ButlerMediumOutlinedButton(
                        modifier = Modifier.weight(testText.length.toFloat()),
                        onClick = {
                            testCredential(ApiKeyCredential(providerUrl, apiKey))
                        },
                        text = {
                            Text(
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState("new_api_key_test_text"),
                                    animatedVisibilityScope = animationScope
                                ).skipToLookaheadSize(),
                                text = testText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState("new_api_key_test_icon"),
                                    animatedVisibilityScope = animationScope
                                ),
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null
                            )
                        }
                    )
                    val saveText = stringResource(Res.string.save)
                    ButlerMediumSolidButton(
                        modifier = Modifier
                            .weight(saveText.length.toFloat())
                            .sharedBounds(
                                sharedContentState = rememberSharedContentState("new_api_key_button_bounds"),
                                animatedVisibilityScope = animationScope,
                                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                            ).then(with(animationScope) {
                                Modifier.animateEnterExit(
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                )
                            }),
                        onClick = {
                            if (apiKey.isBlank()) apiKeyFieldBlank = true
                            if (providerUrl.isBlank()) providerUrlFieldBlank = true
                            if (apiKey.isBlank() || providerUrl.isBlank()) return@ButlerMediumSolidButton
                            saveCredential(ApiKeyCredential(providerUrl, apiKey))
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState("new_api_key_icon"),
                                    animatedVisibilityScope = animationScope
                                ),
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState("new_api_key_text"),
                                    animatedVisibilityScope = animationScope
                                ).skipToLookaheadSize(),
                                text = stringResource(Res.string.save)
                            )
                        }
                    )
                }
                ButlerMediumOutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onBack,
                    text = {
                        Text(text = stringResource(Res.string.back))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                )
                AnimatedVisibility(visible = models.isNotEmpty()) {
                    ApiKeyTestModelListing(
                        modifier = Modifier
                            .animateContentSize()
                            .fillMaxWidth(),
                        models = models
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ApiKeyItemFields(
    modifier: Modifier = Modifier,
    key: Int? = null,
    providerUrl: String,
    apiKey: String,
    showProviderUrlError: Boolean = false,
    showApiKeyError: Boolean = false,
    onProviderUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope,
) = with(sharedTransitionScope) {
    Column(
        modifier = modifier.width(IntrinsicSize.Max),
        verticalArrangement = verticalArrangement
    ) {
        ButlerTextField(
            modifier = Modifier.fillMaxWidth().sharedElement(
                sharedContentState = rememberSharedContentState(key?.let { "api_key_provider_url_$it" } ?: "new_api_key_provider_url"),
                animatedVisibilityScope = animationScope
            ),
            value = providerUrl,
            isOutlined = false,
            onValueChange = onProviderUrlChanged,
            label = {
                Text(text = stringResource(Res.string.provider_url))
            },
            isError = showProviderUrlError,
            supportingText = if (showProviderUrlError) {
                {
                    Text(text = stringResource(Res.string.required))
                }
            } else null
        )
        ButlerTextField(
            modifier = Modifier.fillMaxWidth().sharedElement(
                sharedContentState = rememberSharedContentState(key?.let { "api_key_api_key_$it" } ?: "new_api_key_api_key"),
                animatedVisibilityScope = animationScope
            ),
            value = apiKey,
            isOutlined = false,
            onValueChange = onApiKeyChanged,
            label = {
                Text(text = stringResource(Res.string.api_key))
            },
            isError = showApiKeyError,
            supportingText = if (showApiKeyError) { {
                Text(text = stringResource(Res.string.required))
            } } else null
        )
    }
}
