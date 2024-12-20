package illyan.butler.ui.apikey

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import illyan.butler.core.ui.components.ButlerElevatedCard
import illyan.butler.domain.model.ApiKeyCredential
import illyan.butler.domain.model.DomainModel
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.add_api_key
import illyan.butler.generated.resources.add_credentials
import illyan.butler.generated.resources.unknown
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Serializable
data class ApiKeyCredentialEditItem(val item: ApiKeyCredential)

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
        models = models,
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
    val navController = rememberNavController()
    SharedTransitionScope {
        NavHost(
            navController = navController,
            startDestination = ApiKeyCredentialList
        ) {
            composable<ApiKeyCredentialList> {
                ApiKeyCredentialList(
                    modifier = modifier,
                    credentials = credentials,
                    createNewCredential = { navController.navigate(NewApiKeyCredential) },
                    sharedTransitionScope = this@SharedTransitionScope,
                    animationScope = this@composable
                )
            }
            composable<ApiKeyCredentialEditItem> {
                val item = it.toRoute<ApiKeyCredential>()
                EditApiKeyCredential(
                    item = item
                )
            }
            composable<NewApiKeyCredential> {
                NewApiKeyCredential()
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
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope
) = with(sharedTransitionScope) {
    val lazyGridState = rememberLazyGridState()
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Adaptive(128.dp),
        state = lazyGridState
    ) {
        item("new_api_key") {
            NewApiKeyCredentialGridItem(
                modifier = Modifier.sharedBounds(
                    sharedContentState = rememberSharedContentState("new_api_key_bounds"),
                    animatedVisibilityScope = animationScope
                ),
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
            items(credentials, key = { it.providerUrl }) { item ->
                ApiKeyCredentialGridItem(
                    item = item,
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
    SharedTransitionScope {
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
                        ),
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
                        ),
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
) {
    ButlerElevatedCard {
        Column(
            modifier = modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier.size(64.dp),
                imageVector = Icons.Rounded.Add,
                contentDescription = null
            )
            Text(
                text = stringResource(Res.string.add_api_key),
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ApiKeyCredentialGridItem(
    modifier: Modifier = Modifier,
    item: ApiKeyCredential,
    editItem: () -> Unit = {},
    sharedTransitionScope: SharedTransitionScope,
    animationScope: AnimatedContentScope
) = with(sharedTransitionScope) {
    Box {
        ButlerCard(
            onClick = editItem
        ) {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState("api_key_name_${item.providerUrl}"),
                        animatedVisibilityScope = animationScope
                    ),
                    text = item.name ?: stringResource(Res.string.unknown),
                )
                Text(
                    modifier = Modifier.sharedElement(
                        state = rememberSharedContentState("api_key_provider_url_${item.providerUrl}"),
                        animatedVisibilityScope = animationScope
                    ),
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
    saveCredential: (ApiKeyCredential) -> Unit = {},
) {
    var name by rememberSaveable(item.name) { mutableStateOf(item.name) }
    var providerUrl by rememberSaveable(item.providerUrl) { mutableStateOf(item.providerUrl) }
    var apiKey by rememberSaveable(item.apiKey) { mutableStateOf(item.apiKey) }
}

@Composable
fun NewApiKeyCredential(
    modifier: Modifier = Modifier,
    saveCredential: (ApiKeyCredential) -> Unit = {},
    testCredential: (ApiKeyCredential) -> Unit = {}
) {
    var name by rememberSaveable { mutableStateOf("") }
    var providerUrl by rememberSaveable { mutableStateOf("") }
    var apiKey by rememberSaveable { mutableStateOf("") }
}
