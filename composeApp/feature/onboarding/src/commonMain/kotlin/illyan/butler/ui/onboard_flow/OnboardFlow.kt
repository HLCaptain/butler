package illyan.butler.ui.onboard_flow

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.CloudCircle
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import androidx.window.core.layout.WindowHeightSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import illyan.butler.core.ui.components.ButlerCard
import illyan.butler.core.ui.components.ButlerCardDefaults
import illyan.butler.core.ui.components.ButlerLargeOutlinedButton
import illyan.butler.core.ui.components.ButlerLargeSolidButton
import illyan.butler.core.ui.utils.BackHandler
import illyan.butler.core.ui.utils.ReverseLayoutDirection
import illyan.butler.ui.apikey.ApiKey
import illyan.butler.ui.onboard_flow.AuthItemDefaults.AuthItemDescription
import illyan.butler.ui.onboard_flow.AuthItemDefaults.AuthItemIcon
import illyan.butler.ui.onboard_flow.AuthItemDefaults.AuthItemTitle
import illyan.butler.ui.onboard_flow.AuthItemDefaults.ConsList
import illyan.butler.ui.onboard_flow.AuthItemDefaults.ProsList
import illyan.butler.ui.server.auth_flow.AuthFlow
import kotlinx.serialization.Serializable

object SizeClass {
    const val COMPACT = 0
    const val MEDIUM = 1
    const val EXPANDED = 2
}

val WindowAdaptiveInfo.widthSizeClass: Int
    get() = when (windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> SizeClass.COMPACT
        WindowWidthSizeClass.MEDIUM -> SizeClass.MEDIUM
        WindowWidthSizeClass.EXPANDED -> SizeClass.EXPANDED
        else -> SizeClass.COMPACT
    }

val WindowAdaptiveInfo.heightSizeClass: Int
    get() = when (windowSizeClass.windowHeightSizeClass) {
        WindowHeightSizeClass.COMPACT -> SizeClass.COMPACT
        WindowHeightSizeClass.MEDIUM -> SizeClass.MEDIUM
        WindowHeightSizeClass.EXPANDED -> SizeClass.EXPANDED
        else -> SizeClass.COMPACT
    }

@Serializable
data class Start(val widthSizeClass: Int, val heightSizeClass: Int)

@Serializable
data class AuthItemDestination(
    val index: Int,
    val widthSizeClass: Int,
    val heightSizeClass: Int
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun OnboardFlow(
    authSuccessEnded: () -> Unit,
) {
    val navController = rememberNavController()
    val animationTime = 200

    BackHandler(enabled = navController.previousBackStackEntry != null) {
        navController.navigateUp()
    }

    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Compact: 3 segmented vertical layout listing
            // Others: 3 segmented horizontal layout listing
            // - Local LLM
            // - Hosted server
            // - OpenAI API

            // Vertical layout:
            // Each item is an extendable card with additional information.
            // Extending a card transitions it into fullscreen.
            // On the fullscreen card, select the item to begin
            // the authentication process for that item.

            // Horizontal layout:
            // Each item is an extendable card with additional information.
            // Extending a card transitions it into fullscreen.
            // On the fullscreen card, select the item to begin
            // the authentication process for that item.

            val authItems = listOf(
                AuthItem(
                    title = "Local LLM",
                    description = "Authenticate with a local LLM",
                    image = Icons.Rounded.Smartphone,
                    pros = listOf("No network required", "No server required"),
                    cons = listOf(
                        "No chats are saved remotely",
                        "No remote access",
                        "Need to download LLM model"
                    ),
                    enabled = false
                ),
                AuthItem(
                    title = "Hosted server",
                    description = "Authenticate with a hosted server",
                    image = Icons.Rounded.CloudCircle,
                    pros = listOf(
                        "Chats are saved on the cloud",
                        "Can be self hosted",
                        "More models available"
                    ),
                    cons = listOf(
                        "Requires network connection",
                        "Requires server setup",
                        "Server may be down"
                    ),
                    enabled = true
                ),
                AuthItem(
                    title = "OpenAI API",
                    description = "Authenticate with the OpenAI API",
                    image = Icons.Rounded.CloudQueue,
                    pros = listOf(
                        "No self-hosted server required",
                        "Access OpenAI API with custom host and API key"
                    ),
                    cons = listOf("Requires network connection", "Requires API key from provider"),
                    enabled = true
                )
            )
            SharedTransitionLayout {
                val windowAdaptiveInfo = currentWindowAdaptiveInfo()
                val windowWidthSizeClass = windowAdaptiveInfo.windowSizeClass.windowWidthSizeClass
                val windowHeightSizeClass = windowAdaptiveInfo.windowSizeClass.windowHeightSizeClass
                LaunchedEffect(windowWidthSizeClass, windowHeightSizeClass) {
                    when {
                        navController.currentDestination?.hasRoute<Start>() == true -> {
                            navController.navigate(
                                Start(
                                    windowAdaptiveInfo.widthSizeClass,
                                    windowAdaptiveInfo.heightSizeClass
                                )
                            ) {
                                popUpTo<Start> {
                                    inclusive = true
                                }
                            }
                        }
                        navController.currentDestination?.hasRoute<AuthItemDestination>() == true -> {
                            val index = navController.currentBackStackEntry?.toRoute<AuthItemDestination>()?.index
                            navController.navigate(
                                Start(
                                    windowAdaptiveInfo.widthSizeClass,
                                    windowAdaptiveInfo.heightSizeClass
                                )
                            ) {
                                popUpTo<Start> {
                                    inclusive = true
                                }
                            }
                            navController.navigate(
                                AuthItemDestination(
                                    index ?: 0,
                                    windowAdaptiveInfo.widthSizeClass,
                                    windowAdaptiveInfo.heightSizeClass
                                )
                            )
                        }
                    }
                }
                NavHost(
                    navController = navController,
                    contentAlignment = Alignment.Center,
                    sizeTransform = { SizeTransform(clip = true) },
                    startDestination = Start(
                        windowAdaptiveInfo.widthSizeClass,
                        windowAdaptiveInfo.heightSizeClass
                    ),
                    enterTransition = { slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime)) },
                    popEnterTransition = { slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime)) },
                    exitTransition = { slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)) },
                    popExitTransition = { slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)) }
                ) {
                    composable<Start> {
                        val isWidthCompact = it.toRoute<Start>().widthSizeClass == SizeClass.COMPACT
                        val isHeightCompact = it.toRoute<Start>().heightSizeClass != SizeClass.EXPANDED
                        LaunchedEffect(Unit) {
                            if (windowAdaptiveInfo.widthSizeClass != it.toRoute<Start>().widthSizeClass ||
                                windowAdaptiveInfo.heightSizeClass != it.toRoute<Start>().heightSizeClass
                            ) {
                                navController.navigate(
                                    Start(
                                        windowAdaptiveInfo.widthSizeClass,
                                        windowAdaptiveInfo.heightSizeClass
                                    )
                                ) {
                                    popUpTo<Start> {
                                        inclusive = true
                                    }
                                }
                            }
                        }

                        Box(
                            modifier = Modifier.systemBarsPadding(),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isWidthCompact) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    if (isHeightCompact) {
                                        authItems.forEachIndexed { index, item ->
                                            AuthItemCompactCard(
                                                modifier = Modifier,
                                                key = index,
                                                item = item,
                                                sharedTransitionScope = this@SharedTransitionLayout,
                                                animatedVisibilityScope = this@composable,
                                                onClick = {
                                                    navController.navigate(
                                                        AuthItemDestination(
                                                            index,
                                                            windowAdaptiveInfo.widthSizeClass,
                                                            windowAdaptiveInfo.heightSizeClass
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    } else {
                                        authItems.forEachIndexed { index, item ->
                                            AuthItemHorizontalCard(
                                                modifier = Modifier,
                                                key = index,
                                                item = item,
                                                sharedTransitionScope = this@SharedTransitionLayout,
                                                animatedVisibilityScope = this@composable,
                                                onClick = {
                                                    navController.navigate(
                                                        AuthItemDestination(
                                                            index,
                                                            windowAdaptiveInfo.widthSizeClass,
                                                            windowAdaptiveInfo.heightSizeClass
                                                        )
                                                    )
                                                }
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    authItems.forEachIndexed { index, item ->
                                        AuthItemVerticalCard(
                                            key = index,
                                            item = item,
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedVisibilityScope = this@composable,
                                            onClick = {
                                                navController.navigate(
                                                    AuthItemDestination(
                                                        index,
                                                        windowAdaptiveInfo.widthSizeClass,
                                                        windowAdaptiveInfo.heightSizeClass
                                                    )
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    composable<AuthItemDestination> {
                        LaunchedEffect(Unit) {
                            if (windowAdaptiveInfo.widthSizeClass != it.toRoute<Start>().widthSizeClass ||
                                windowAdaptiveInfo.heightSizeClass != it.toRoute<Start>().heightSizeClass
                            ) {
                                navController.navigate(
                                    AuthItemDestination(
                                        it.toRoute<AuthItemDestination>().index,
                                        windowAdaptiveInfo.widthSizeClass,
                                        windowAdaptiveInfo.heightSizeClass
                                    )
                                ) {
                                    popUpTo<AuthItemDestination> {
                                        inclusive = true
                                    }
                                }
                            }
                        }
                        val index = it.toRoute<AuthItemDestination>().index
                        val isWidthCompact = it.toRoute<AuthItemDestination>().widthSizeClass == SizeClass.COMPACT
                        if (isWidthCompact) {
                            AuthItemCompactFullscreen(
                                modifier = Modifier,
                                key = index,
                                item = authItems[index],
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable,
                                onNext = { navController.navigate(
                                    when (index) {
                                        0 -> "local_llm"
                                        1 -> "hosted_server"
                                        2 -> "openai_api"
                                        else -> "local_llm"
                                    }
                                ) },
                                onClose = { navController.navigateUp() }
                            )
                        } else {
                            AuthItemExpandedFullscreen(
                                modifier = Modifier,
                                key = index,
                                item = authItems[index],
                                sharedTransitionScope = this@SharedTransitionLayout,
                                animatedVisibilityScope = this@composable,
                                onNext = { navController.navigate(
                                    when (index) {
                                        0 -> "local_llm"
                                        1 -> "hosted_server"
                                        2 -> "openai_api"
                                        else -> "local_llm"
                                    }
                                ) },
                                onClose = { navController.navigateUp() }
                            )
                        }

                    }
                    composable("local_llm") {
                        Text("Local LLM")
                    }
                    composable("hosted_server") {
                        AuthFlow(
                            authSuccessEnded = authSuccessEnded,
                            onBack = navController::navigateUp
                        )
                    }
                    composable("openai_api") {
                        ApiKey(
                            onBack = navController::navigateUp,
                            onNext = authSuccessEnded
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ColumnScope.AuthItemCompactCard(
    modifier: Modifier = Modifier,
    key: Int,
    item: AuthItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    ButlerCard(
        modifier = modifier.weight(1f).sharedBounds(
            rememberSharedContentState(key = "card$key"),
            animatedVisibilityScope = animatedVisibilityScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
        ),
        onClick = onClick,
        enabled = item.enabled,
        contentPadding = ButlerCardDefaults.CompactContentPadding
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AuthItemIcon(
                    modifier = Modifier.size(64.dp),
                    icon = item.image,
                    animatedVisibilityScope = animatedVisibilityScope,
                    key = key
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Column {
                        AuthItemTitle(
                            modifier = Modifier,
                            title = item.title,
                            animatedVisibilityScope = animatedVisibilityScope,
                            key = key
                        )
                        AuthItemDescription(
                            modifier = Modifier,
                            description = item.description,
                            animatedVisibilityScope = animatedVisibilityScope,
                            key = key
                        )
                        ProsList(
                            modifier = Modifier.height(0.dp),
                            pros = item.pros,
                            animatedVisibilityScope = animatedVisibilityScope,
                            enabled = item.enabled,
                            key = key
                        )
                        ConsList(
                            modifier = Modifier.height(0.dp),
                            cons = item.cons,
                            animatedVisibilityScope = animatedVisibilityScope,
                            enabled = item.enabled,
                            key = key
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "select-$key"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ).padding(8.dp),
                    onClick = onClick,
                    enabled = item.enabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ColumnScope.AuthItemHorizontalCard(
    modifier: Modifier = Modifier,
    key: Int,
    item: AuthItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    ButlerCard(
        modifier = modifier.weight(1f).sharedBounds(
            rememberSharedContentState(key = "card$key"),
            animatedVisibilityScope = animatedVisibilityScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
        ),
        onClick = onClick,
        enabled = item.enabled,
        contentPadding = ButlerCardDefaults.CompactContentPadding
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuthItemIcon(
                    modifier = Modifier.size(64.dp),
                    icon = item.image,
                    animatedVisibilityScope = animatedVisibilityScope,
                    key = key
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AuthItemTitle(
                        modifier = Modifier,
                        title = item.title,
                        animatedVisibilityScope = animatedVisibilityScope,
                        key = key
                    )
                    AuthItemDescription(
                        modifier = Modifier,
                        description = item.description,
                        animatedVisibilityScope = animatedVisibilityScope,
                        key = key
                    )
                    ProsList(
                        modifier = Modifier,
                        pros = item.pros,
                        animatedVisibilityScope = animatedVisibilityScope,
                        enabled = item.enabled,
                        key = key
                    )
                    ConsList(
                        modifier = Modifier,
                        cons = item.cons,
                        animatedVisibilityScope = animatedVisibilityScope,
                        enabled = item.enabled,
                        key = key
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "select-$key"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ).padding(8.dp),
                    onClick = onClick,
                    enabled = item.enabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RowScope.AuthItemVerticalCard(
    modifier: Modifier = Modifier,
    key: Int,
    item: AuthItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    ButlerCard(
        modifier = modifier.weight(1f).sharedBounds(
            rememberSharedContentState(key = "card$key"),
            animatedVisibilityScope = animatedVisibilityScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
        ),
        onClick = onClick,
        enabled = item.enabled,
        contentPadding = ButlerCardDefaults.CompactContentPadding
    ) {
        ReverseLayoutDirection {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ReverseLayoutDirection {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AuthItemIcon(
                                modifier = Modifier.size(72.dp),
                                icon = item.image,
                                animatedVisibilityScope = animatedVisibilityScope,
                                key = key
                            )
                            AuthItemTitle(
                                modifier = Modifier,
                                title = item.title,
                                textAlign = TextAlign.Center,
                                animatedVisibilityScope = animatedVisibilityScope,
                                key = key
                            )
                            AuthItemDescription(
                                modifier = Modifier,
                                description = item.description,
                                textAlign = TextAlign.Center,
                                animatedVisibilityScope = animatedVisibilityScope,
                                key = key
                            )
                            Column {
                                ProsList(
                                    modifier = Modifier,
                                    pros = item.pros,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    enabled = item.enabled,
                                    key = key
                                )
                                ConsList(
                                    modifier = Modifier,
                                    cons = item.cons,
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    enabled = item.enabled,
                                    key = key
                                )
                            }
                        }
                    }
                    ButlerLargeSolidButton(
                        modifier = Modifier.fillMaxWidth().sharedElement(
                            rememberSharedContentState(key = "select-$key"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ).padding(8.dp),
                        onClick = onClick,
                        enabled = item.enabled,
                    ) {
                        Text(if (item.enabled) "Select" else "Unavailable")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AuthItemCompactFullscreen(
    modifier: Modifier = Modifier,
    key: Int,
    item: AuthItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNext: () -> Unit,
    onClose: () -> Unit
) = with(sharedTransitionScope) {
    ButlerCard(
        modifier = modifier.sharedBounds(
            rememberSharedContentState(key = "card$key"),
            animatedVisibilityScope = animatedVisibilityScope,
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
                modifier = Modifier.width(intrinsicSize = IntrinsicSize.Max),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AuthItemIcon(
                    modifier = Modifier.size(128.dp),
                    icon = item.image,
                    animatedVisibilityScope = animatedVisibilityScope,
                    key = key
                )
                AuthItemTitle(
                    modifier = Modifier,
                    title = item.title,
                    animatedVisibilityScope = animatedVisibilityScope,
                    key = key
                )
                AuthItemDescription(
                    modifier = Modifier,
                    description = item.description,
                    animatedVisibilityScope = animatedVisibilityScope,
                    key = key
                )
                Column {
                    ProsList(
                        modifier = Modifier,
                        pros = item.pros,
                        animatedVisibilityScope = animatedVisibilityScope,
                        enabled = item.enabled,
                        key = key
                    )
                    ConsList(
                        modifier = Modifier,
                        cons = item.cons,
                        animatedVisibilityScope = animatedVisibilityScope,
                        enabled = item.enabled,
                        key = key
                    )
                }
                Column(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ButlerLargeOutlinedButton(
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "close-$key"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                        onClick = onClose,
                        enabled = true
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Close")
                        }
                    }
                    ButlerLargeSolidButton(
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "select-$key"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                        onClick = onNext,
                        enabled = item.enabled
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (item.enabled) "Select" else "Unavailable")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AuthItemExpandedFullscreen(
    modifier: Modifier = Modifier,
    key: Int,
    item: AuthItem,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNext: () -> Unit,
    onClose: () -> Unit
) = with(sharedTransitionScope) {
    ButlerCard(
        modifier = modifier.sharedBounds(
            rememberSharedContentState(key = "card$key"),
            animatedVisibilityScope = animatedVisibilityScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds
        ),
        shape = RoundedCornerShape(0.dp),
        contentPadding = ButlerCardDefaults.CompactContentPadding
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AuthItemIcon(
                    modifier = Modifier.size(128.dp),
                    icon = item.image,
                    animatedVisibilityScope = animatedVisibilityScope,
                    key = key
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AuthItemTitle(
                        modifier = Modifier,
                        title = item.title,
                        animatedVisibilityScope = animatedVisibilityScope,
                        key = key
                    )
                    AuthItemDescription(
                        modifier = Modifier,
                        description = item.description,
                        animatedVisibilityScope = animatedVisibilityScope,
                        key = key
                    )
                    ProsList(
                        modifier = Modifier,
                        pros = item.pros,
                        animatedVisibilityScope = animatedVisibilityScope,
                        enabled = item.enabled,
                        key = key
                    )
                    ConsList(
                        modifier = Modifier,
                        cons = item.cons,
                        animatedVisibilityScope = animatedVisibilityScope,
                        enabled = item.enabled,
                        key = key
                    )
                    Row(
                        modifier = Modifier.padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ButlerLargeOutlinedButton(
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "close-$key"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                            onClick = onClose,
                            enabled = true,
                            text = { Text("Close") }
                        )
                        ButlerLargeSolidButton(
                            modifier = Modifier.sharedElement(
                                rememberSharedContentState(key = "select-$key"),
                                animatedVisibilityScope = animatedVisibilityScope
                            ),
                            onClick = onNext,
                            enabled = item.enabled,
                            text = { Text(if (item.enabled) "Select" else "Unavailable") }
                        )
                    }
                }
            }
        }
    }
}

object AuthItemDefaults {
    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun SharedTransitionScope.ConsList(
        modifier: Modifier = Modifier,
        cons: List<String>,
        animatedVisibilityScope: AnimatedVisibilityScope,
        enabled: Boolean = true,
        key: Int
    ) {
        val textAlpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.5f)
        ProvideTextStyle(
            MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.error.copy(
                    alpha = textAlpha
                )
            )
        ) {
            Column {
                cons.forEachIndexed { index, con ->
                    Row(
                        modifier = modifier.sharedElement(
                            rememberSharedContentState(key = "con$index-$key"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ).skipToLookaheadSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "-")
                        Text(text = con)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun SharedTransitionScope.ProsList(
        modifier: Modifier = Modifier,
        pros: List<String>,
        animatedVisibilityScope: AnimatedVisibilityScope,
        enabled: Boolean = true,
        key: Int
    ) {
        val textAlpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.5f)
        ProvideTextStyle(
            MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.primary.copy(
                    alpha = textAlpha
                )
            )
        ) {
            Column {
                pros.forEachIndexed { index, pro ->
                    Row(
                        modifier = modifier.sharedElement(
                            rememberSharedContentState(key = "pro$index-$key"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ).skipToLookaheadSize(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = "+")
                        Text(text = pro)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun SharedTransitionScope.AuthItemTitle(
        modifier: Modifier = Modifier,
        title: String,
        textAlign: TextAlign = TextAlign.Start,
        animatedVisibilityScope: AnimatedVisibilityScope,
        key: Int
    ) {
        Text(
            modifier = modifier.sharedElement(
                rememberSharedContentState(key = "title-$key"),
                animatedVisibilityScope = animatedVisibilityScope
            ).skipToLookaheadSize(),
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = textAlign
        )
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun SharedTransitionScope.AuthItemDescription(
        modifier: Modifier = Modifier,
        description: String,
        textAlign: TextAlign = TextAlign.Start,
        animatedVisibilityScope: AnimatedVisibilityScope,
        key: Int
    ) {
        Text(
            modifier = modifier.sharedElement(
                rememberSharedContentState(key = "description-$key"),
                animatedVisibilityScope = animatedVisibilityScope
            ).skipToLookaheadSize(),
            text = description,
            style = MaterialTheme.typography.titleMedium,
            textAlign = textAlign
        )
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    @Composable
    fun SharedTransitionScope.AuthItemIcon(
        modifier: Modifier = Modifier,
        icon: ImageVector,
        animatedVisibilityScope: AnimatedVisibilityScope,
        key: Int
    ) {
        Icon(
            modifier = modifier.sharedElement(
                rememberSharedContentState(key = "icon-$key"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
            imageVector = icon,
            contentDescription = null
        )
    }
}
