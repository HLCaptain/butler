package illyan.butler.ui.apikey

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerElevatedCard
import illyan.butler.core.ui.components.ButlerMediumOutlinedButton
import illyan.butler.core.ui.components.ButlerMediumSolidButton
import illyan.butler.core.ui.components.ButlerTextField
import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.add_api_key
import illyan.butler.generated.resources.add_credentials
import illyan.butler.generated.resources.api_key
import illyan.butler.generated.resources.back
import illyan.butler.generated.resources.delete
import illyan.butler.generated.resources.edit
import illyan.butler.generated.resources.name
import illyan.butler.generated.resources.provider_url
import illyan.butler.generated.resources.save
import illyan.butler.generated.resources.test
import illyan.butler.generated.resources.unknown
import kotlinx.serialization.Serializable
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
) {
    val viewModel = koinViewModel<ApiKeyViewModel>()
    val apiKeyCredentials by viewModel.apiKeyCredentials.collectAsState()
    val models by viewModel.modelsForCredential.collectAsState()
    ApiKeyCredential(
        modifier = modifier,
        credentials = apiKeyCredentials,
        models = models.toMap(),
        createNewCredential = viewModel::addApiKeyCredential,
        testApiKeyCredential = viewModel::testEndpointForCredentials,
        deleteCredential = viewModel::deleteApiKeyCredential
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ApiKeyCredential(
    modifier: Modifier = Modifier,
    credentials: List<ApiKeyCredential>?,
    models: Map<ApiKeyCredential, List<DomainModel>>,
    createNewCredential: (ApiKeyCredential) -> Unit = {},
    testApiKeyCredential: (ApiKeyCredential) -> Unit = {},
    deleteCredential: (ApiKeyCredential) -> Unit = {}
) {
    val animationTime = 500
    SharedTransitionLayout {
        val navController = rememberNavController()
        NavHost(
            modifier = modifier,
            navController = navController,
            startDestination = ApiKeyCredentialList,
            contentAlignment = Alignment.Center,
            sizeTransform = { SizeTransform(clip = true) },
            enterTransition = {
                slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(
                    tween(animationTime)
                )
            },
            popEnterTransition = {
                slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(
                    tween(animationTime)
                )
            },
            exitTransition = {
                slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(
                    tween(animationTime)
                )
            },
            popExitTransition = {
                slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(
                    tween(animationTime)
                )
            }
        ) {
            composable<ApiKeyCredentialList> {
                ApiKeyCredentialList(
                    credentials = credentials,
                    createNewCredential = { navController.navigate(NewApiKeyCredential) },
                    editCredential = { index ->
                        credentials?.getOrNull(index)?.let {
                            navController.navigate(ApiKeyCredentialEditItem(it.name, it.providerUrl, it.apiKey, index))
                        }
                    },
                    deleteCredential = deleteCredential,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animationScope = this@composable
                )
            }
            composable<ApiKeyCredentialEditItem> {
                val (item, index) = remember(credentials) {
                    val editItem = it.toRoute<ApiKeyCredentialEditItem>()
                    ApiKeyCredential(
                        name = editItem.name,
                        providerUrl = editItem.providerUrl,
                        apiKey = editItem.apiKey
                    ) to editItem.index
                }
                EditApiKeyCredential(
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
                var testCredential by rememberSaveable { mutableStateOf<ApiKeyCredential?>(null) }
                NewApiKeyCredential(
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
    createNewCredential: () -> Unit,
    editCredential: (Int) -> Unit,
    deleteCredential: (ApiKeyCredential) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope
) {
    val lazyGridState = rememberLazyGridState()
    val minCellSize = 128.dp
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
                modifier = Modifier.animateItem().sizeIn(minWidth = minCellSize, minHeight = minCellSize),
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
                    modifier = Modifier.animateItem().sizeIn(minWidth = minCellSize, minHeight = minCellSize),
                    lazyGridState = lazyGridState
                )
            }
        } else {
            itemsIndexed(credentials.distinctBy { it.providerUrl }, key = { _, item -> item.providerUrl }) { index, item  ->
                ApiKeyCredentialGridItem(
                    modifier = Modifier.animateItem().sizeIn(minWidth = minCellSize, minHeight = minCellSize),
                    item = item,
                    key = index,
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
        targetState = lazyGridState.layoutInfo.maxSpan > 1,
        transitionSpec = {
            val slideSpec = spring<IntOffset>(stiffness = Spring.StiffnessMediumLow, dampingRatio = (Spring.DampingRatioMediumBouncy + Spring.DampingRatioLowBouncy) / 2)
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
                        text = stringResource(Res.string.add_credentials)
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
                        state = rememberSharedContentState("new_api_key_icon"),
                        animatedVisibilityScope = animationScope
                    ),
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null
                )
                Text(
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState("new_api_key_text"),
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
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 16.dp, end = 32.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState("api_key_name_$key"),
                        animatedVisibilityScope = animationScope
                    ).skipToLookaheadSize(),
                    text = item.name.takeIf { !it.isNullOrBlank() } ?: stringResource(Res.string.unknown),
                )
                Text(
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState("api_key_provider_url_$key"),
                        animatedVisibilityScope = animationScope
                    ).skipToLookaheadSize(),
                    text = item.providerUrl.takeIf { it.isNotBlank() } ?: stringResource(Res.string.unknown),
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
                ExposedDropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    matchTextFieldWidth = false
                ) {
                    DropdownMenuItem(
                        onClick = { showMenu = false; editItem() },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(Res.string.edit)) }
                    )
                    DropdownMenuItem(
                        onClick = { showMenu = false; deleteItem() },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null
                            )
                        },
                        text = { Text(stringResource(Res.string.delete)) }
                    )
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
    var name by rememberSaveable(item.name) { mutableStateOf(item.name ?: "") }
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier.width(IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ApiKeyItemFields(
                    name = name,
                    providerUrl = providerUrl,
                    apiKey = apiKey,
                    onNameChanged = { name = it },
                    onProviderUrlChanged = { providerUrl = it },
                    onApiKeyChanged = { apiKey = it },
                    sharedTransitionScope = sharedTransitionScope,
                    animationScope = animationScope
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ButlerMediumOutlinedButton(
                        onClick = {
                            testCredential(ApiKeyCredential(name, providerUrl, apiKey))
                        },
                        text = {
                            Text(
                                text = stringResource(Res.string.test)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState("new_api_key_test_icon"),
                                    animatedVisibilityScope = animationScope
                                ),
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null
                            )
                        }
                    )
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
                        }),
                        onClick = {
                            saveCredential(ApiKeyCredential(name, providerUrl, apiKey))
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState("new_api_key_icon"),
                                    animatedVisibilityScope = animationScope
                                ),
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState("new_api_key_text"),
                                    animatedVisibilityScope = animationScope
                                ).skipToLookaheadSize(),
                                text = stringResource(Res.string.save)
                            )
                        }
                    )
                }
                ButlerMediumOutlinedButton(
                    onClick = onBack,
                    text = {
                        Text(
                            text = stringResource(Res.string.back)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                )
                Column {
                    models.forEach { model ->
                        Text(
                            text = model.displayName
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
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = modifier.width(IntrinsicSize.Min),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ApiKeyItemFields(
                    name = name,
                    providerUrl = providerUrl,
                    apiKey = apiKey,
                    onNameChanged = { name = it },
                    onProviderUrlChanged = { providerUrl = it },
                    onApiKeyChanged = { apiKey = it },
                    sharedTransitionScope = sharedTransitionScope,
                    animationScope = animationScope
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    ButlerMediumOutlinedButton(
                        onClick = {
                            testCredential(ApiKeyCredential(name, providerUrl, apiKey))
                        },
                        text = {
                            Text(
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState("new_api_key_test_text"),
                                    animatedVisibilityScope = animationScope
                                ).skipToLookaheadSize(),
                                text = stringResource(Res.string.test)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState("new_api_key_test_icon"),
                                    animatedVisibilityScope = animationScope
                                ),
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = null
                            )
                        }
                    )
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
                        }),
                        onClick = {
                            saveCredential(ApiKeyCredential(name, providerUrl, apiKey))
                        },
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState("new_api_key_icon"),
                                    animatedVisibilityScope = animationScope
                                ),
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        },
                        text = {
                            Text(
                                modifier = Modifier.sharedElement(
                                    state = rememberSharedContentState("new_api_key_text"),
                                    animatedVisibilityScope = animationScope
                                ).skipToLookaheadSize(),
                                text = stringResource(Res.string.save)
                            )
                        }
                    )
                }
                ButlerMediumOutlinedButton(
                    onClick = onBack,
                    text = {
                        Text(
                            text = stringResource(Res.string.back)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                )
                Column {
                    models.forEach { model ->
                        Text(
                            text = model.displayName
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ApiKeyItemFields(
    modifier: Modifier = Modifier,
    name: String,
    providerUrl: String,
    apiKey: String,
    key: Int? = null,
    onNameChanged: (String) -> Unit,
    onProviderUrlChanged: (String) -> Unit,
    onApiKeyChanged: (String) -> Unit,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope,
) = with(sharedTransitionScope) {
    Column(
        modifier = modifier.width(IntrinsicSize.Min),
        verticalArrangement = verticalArrangement
    ) {
        ButlerTextField(
            modifier = Modifier.sharedElement(
                state = rememberSharedContentState(key?.let { "api_key_name_$it" } ?: "new_api_key_name"),
                animatedVisibilityScope = animationScope
            ),
            value = name,
            isOutlined = false,
            onValueChange = onNameChanged,
            label = {
                Text(text = stringResource(Res.string.name))
            },
        )
        ButlerTextField(
            modifier = Modifier.sharedElement(
                state = rememberSharedContentState(key?.let { "api_key_provider_url_$it" } ?: "new_api_key_provider_url"),
                animatedVisibilityScope = animationScope
            ),
            value = providerUrl,
            isOutlined = false,
            onValueChange = onProviderUrlChanged,
            label = {
                Text(text = stringResource(Res.string.provider_url))
            },
        )
        ButlerTextField(
            modifier = Modifier.sharedElement(
                state = rememberSharedContentState(key?.let { "api_key_api_key_$it" } ?: "new_api_key_api_key"),
                animatedVisibilityScope = animationScope
            ),
            value = apiKey,
            isOutlined = false,
            onValueChange = onApiKeyChanged,
            label = {
                Text(text = stringResource(Res.string.api_key))
            },
        )
    }
}
