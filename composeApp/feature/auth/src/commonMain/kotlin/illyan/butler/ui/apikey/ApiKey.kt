package illyan.butler.ui.apikey

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
    val apiKey: String
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
        testApiKeyCredential = viewModel::testEndpointForCredentials
    )
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ApiKeyCredential(
    modifier: Modifier = Modifier,
    credentials: List<ApiKeyCredential>?,
    models: Map<ApiKeyCredential, List<DomainModel>>,
    createNewCredential: (ApiKeyCredential) -> Unit = {},
    testApiKeyCredential: (ApiKeyCredential) -> Unit = {}
) {
    val animationTime = 2000
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
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animationScope = this@composable
                )
            }
            composable<ApiKeyCredentialEditItem> {
                val item = remember {
                    ApiKeyCredential(
                        name = it.toRoute<ApiKeyCredentialEditItem>().name,
                        providerUrl = it.toRoute<ApiKeyCredentialEditItem>().providerUrl,
                        apiKey = it.toRoute<ApiKeyCredentialEditItem>().apiKey
                    )
                }
                EditApiKeyCredential(
                    item = item,
                    models = models[item] ?: emptyList(),
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
    createNewCredential: () -> Unit = {},
    editCredential: (ApiKeyCredential) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope
) = with(sharedTransitionScope) {
    val lazyGridState = rememberLazyGridState()
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(128.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        state = lazyGridState
    ) {
        item("new_api_key") {
            NewApiKeyCredentialGridItem(
                createNewCredential = createNewCredential,
                sharedTransitionScope = sharedTransitionScope,
                animationScope = animationScope
            )
        }
        if (credentials.isNullOrEmpty()) {
            item(key = "no_api_keys") {
                NoApiKeyCredentialGridItem(lazyGridState = lazyGridState)
            }
        } else {
            itemsIndexed(credentials, key = { _, item -> item.providerUrl }) { index, item  ->
                ApiKeyCredentialGridItem(
                    modifier = Modifier.sharedBounds(
                        sharedContentState = rememberSharedContentState("api_key_bounds_$index"),
                        animatedVisibilityScope = animationScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
                    ),
                    item = item,
                    key = index,
                    editItem = { editCredential(item) },
                    sharedTransitionScope = sharedTransitionScope,
                    animationScope = animationScope
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NoApiKeyCredentialGridItem(
    modifier: Modifier = Modifier,
    lazyGridState: LazyGridState
) {
    SharedTransitionLayout {
        AnimatedContent(
            modifier = modifier,
            targetState = lazyGridState.layoutInfo.maxSpan
        ) { maxSpan ->
            val nestedAnimatedScope = this // Nested scope to animate elements in SharedTransition
            if (maxSpan > 1) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier.size(64.dp).sharedElement(
                            state = rememberSharedContentState("no_api_keys_icon"),
                            animatedVisibilityScope = nestedAnimatedScope
                        ),
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.sharedElement(
                            state = rememberSharedContentState("no_api_keys_text"),
                            animatedVisibilityScope = nestedAnimatedScope
                        ).skipToLookaheadSize(),
                        text = stringResource(Res.string.add_credentials)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.size(64.dp).sharedElement(
                            state = rememberSharedContentState("no_api_keys_icon"),
                            animatedVisibilityScope = nestedAnimatedScope
                        ),
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = null
                    )
                    Text(
                        modifier = Modifier.sharedElement(
                            state = rememberSharedContentState("no_api_keys_text"),
                            animatedVisibilityScope = nestedAnimatedScope
                        ).skipToLookaheadSize(),
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
        modifier = modifier.sharedBounds(
            sharedContentState = rememberSharedContentState("new_api_key_bounds"),
            animatedVisibilityScope = animationScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
        ),
        onClick = createNewCredential,
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize().sharedBounds(
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ApiKeyCredentialGridItem(
    modifier: Modifier = Modifier,
    item: ApiKeyCredential,
    key: Int,
    editItem: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope
) = with(sharedTransitionScope) {
    Box {
        ButlerCard(
            modifier = Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState("api_key_bounds_$key"),
                animatedVisibilityScope = animationScope,
                resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
            ),
            onClick = editItem
        ) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState("api_key_name_$key"),
                        animatedVisibilityScope = animationScope
                    ).skipToLookaheadSize(),
                    text = item.name ?: stringResource(Res.string.unknown),
                )
                Text(
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState("api_key_provider_url_$key"),
                        animatedVisibilityScope = animationScope
                    ).skipToLookaheadSize(),
                    text = item.providerUrl,
                )
            }
        }
        IconButton(
            modifier = Modifier.align(Alignment.TopEnd),
            onClick = editItem,
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null
            )
        }
    }
}

@Composable
fun EditApiKeyCredential(
    modifier: Modifier = Modifier,
    item: ApiKeyCredential,
    models: List<DomainModel>,
    saveCredential: (ApiKeyCredential) -> Unit = {},
) {
    var name by rememberSaveable(item.name) { mutableStateOf(item.name) }
    var providerUrl by rememberSaveable(item.providerUrl) { mutableStateOf(item.providerUrl) }
    var apiKey by rememberSaveable(item.apiKey) { mutableStateOf(item.apiKey) }
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
                modifier = modifier.width(IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ButlerTextField(
                    value = name,
                    isOutlined = false,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            modifier = Modifier.sharedElement(
                                state = rememberSharedContentState("new_api_key_name"),
                                animatedVisibilityScope = animationScope
                            ).skipToLookaheadSize(),
                            text = stringResource(Res.string.name)
                        )
                    },
                )
                ButlerTextField(
                    value = providerUrl,
                    isOutlined = false,
                    onValueChange = { providerUrl = it },
                    label = {
                        Text(
                            modifier = Modifier.sharedElement(
                                state = rememberSharedContentState("new_api_key_provider_url"),
                                animatedVisibilityScope = animationScope
                            ).skipToLookaheadSize(),
                            text = stringResource(Res.string.provider_url)
                        )
                    },
                )
                ButlerTextField(
                    value = apiKey,
                    isOutlined = false,
                    onValueChange = { apiKey = it },
                    label = {
                        Text(
                            modifier = Modifier.sharedElement(
                                state = rememberSharedContentState("new_api_key_api_key"),
                                animatedVisibilityScope = animationScope
                            ).skipToLookaheadSize(),
                            text = stringResource(Res.string.api_key)
                        )
                    },
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
