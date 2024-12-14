package illyan.butler.ui.onboard_flow

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowWidthSizeClass
import illyan.butler.core.ui.components.ButlerLargeOutlinedButton
import illyan.butler.core.ui.components.ButlerLargeSolidButton
import illyan.butler.core.ui.components.ButlerMediumTextButton

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun OnboardFlow(
    authSuccessEnded: () -> Unit,
) {
    val navController = rememberNavController()
    val animationTime = 200
    Column(
        modifier = Modifier.fillMaxHeight(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            NavHost(
                navController = navController,
                contentAlignment = Alignment.Center,
                sizeTransform = { SizeTransform(clip = false) },
                startDestination = "start",
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
                composable("start") {
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

                    val authItemTitles = listOf("Local LLM", "Hosted server", "OpenAI API")
                    val authItemDescriptions = listOf(
                        "Authenticate with a local LLM",
                        "Authenticate with a hosted server",
                        "Authenticate with the OpenAI API"
                    )
                    val authItemIcons = listOf(
                        Icons.Rounded.AccountCircle,
                        Icons.Rounded.AccountCircle,
                        Icons.Rounded.AccountCircle
                    )
                    val authItemPros = listOf(
                        listOf("No network required", "No server required"),
                        listOf(
                            "Chats are saved on the cloud",
                            "Can be self hosted",
                            "More models available"
                        ),
                        listOf(
                            "No self-hosted server required",
                            "Access OpenAI API with custom host and API key"
                        )
                    )
                    val authItemCons = listOf(
                        listOf(
                            "No chats are saved remotely",
                            "No remote access",
                            "Need to download LLM model"
                        ),
                        listOf(
                            "Requires network connection",
                            "Requires server setup",
                            "Server may be down"
                        ),
                        listOf("Requires network connection", "Requires API key from provider")
                    )

                    var selectedOption by rememberSaveable { mutableStateOf(-1) }
                    SharedTransitionLayout {
                        val windowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass
                        AnimatedContent(targetState = selectedOption to windowWidthSizeClass) { (selected, sizeClass) ->
                            val isCompact = sizeClass == WindowWidthSizeClass.COMPACT
                            if (selected >= 0) {
                                if (isCompact) {
                                    AuthItemCompactFullscreen(
                                        modifier = Modifier.sharedBounds(
                                            rememberSharedContentState(key = "card$selected"),
                                            animatedVisibilityScope = this@AnimatedContent
                                        ),
                                        item = selected,
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        animatedVisibilityScope = this@AnimatedContent,
                                        icon = authItemIcons[selected],
                                        title = authItemTitles[selected],
                                        description = authItemDescriptions[selected],
                                        pros = authItemPros[selected],
                                        cons = authItemCons[selected],
                                        onNext = { navController.navigate("hosted_server") },
                                        onClose = { selectedOption = -1 }
                                    )
                                } else {
                                    AuthItemExpandedFullscreen(
                                        modifier = Modifier.sharedBounds(
                                            rememberSharedContentState(key = "card$selected"),
                                            animatedVisibilityScope = this@AnimatedContent
                                        ),
                                        item = selected,
                                        sharedTransitionScope = this@SharedTransitionLayout,
                                        animatedVisibilityScope = this@AnimatedContent,
                                        icon = authItemIcons[selected],
                                        title = authItemTitles[selected],
                                        description = authItemDescriptions[selected],
                                        pros = authItemPros[selected],
                                        cons = authItemCons[selected],
                                        onNext = { navController.navigate("hosted_server") },
                                        onClose = { selectedOption = -1 }
                                    )
                                }
                            } else if (isCompact) {
                                Column {
                                    authItemTitles.forEachIndexed { index, title ->
                                        AuthItemCompactCard(
                                            modifier = Modifier.sharedBounds(
                                                rememberSharedContentState(key = "card$index"),
                                                animatedVisibilityScope = this@AnimatedContent
                                            ),
                                            item = index,
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedVisibilityScope = this@AnimatedContent,
                                            icon = authItemIcons[index],
                                            title = title,
                                            description = authItemDescriptions[index],
                                            onClick = { selectedOption = index }
                                        )
                                    }
                                }
                            } else {
                                Row {
                                    authItemTitles.forEachIndexed { index, title ->
                                        AuthItemHorizontalCard(
                                            modifier = Modifier.sharedBounds(
                                                rememberSharedContentState(key = "card$index"),
                                                animatedVisibilityScope = this@AnimatedContent
                                            ),
                                            item = index,
                                            sharedTransitionScope = this@SharedTransitionLayout,
                                            animatedVisibilityScope = this@AnimatedContent,
                                            icon = authItemIcons[index],
                                            title = title,
                                            description = authItemDescriptions[index],
                                            pros = authItemPros[index],
                                            cons = authItemCons[index],
                                            onClick = { selectedOption = index }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                composable("local_llm") {
                    Text("Local LLM")
                }
                composable("hosted_server") {
                    Text("Hosted server")
                }
                composable("openai_api") {
                    Text("OpenAI API")
                }
            }
        }
    }
}


@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AuthItemCompactCard(
    modifier: Modifier = Modifier,
    item: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Row {
            Icon(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "icon-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                imageVector = icon,
                contentDescription = null
            )
            Column {
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "title-$item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    text = title
                )
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "description-$item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    text = description
                )
            }

            IconButton(
                onClick = onClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                    contentDescription = null
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AuthItemHorizontalCard(
    modifier: Modifier = Modifier,
    item: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    icon: ImageVector,
    title: String,
    description: String,
    pros: List<String>,
    cons: List<String>,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Row {
            Icon(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "icon-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                imageVector = icon,
                contentDescription = null
            )
            Column {
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "title-$item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    text = title
                )
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "description-$item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    text = description
                )
                pros.forEachIndexed { index, it ->
                    Text(
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "pro$index-$item"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                        text = it
                    )
                }
                cons.forEachIndexed { index, it ->
                    Text(
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "con$index-$item"),
                            animatedVisibilityScope = animatedVisibilityScope
                        ),
                        text = it
                    )
                }
                ButlerMediumTextButton(
                    onClick = onClick,
                    enabled = true
                ) {
                    Text("Select")
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AuthItemVerticalCard(
    modifier: Modifier = Modifier,
    item: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    icon: ImageVector,
    title: String,
    description: String,
    pros: List<String>,
    cons: List<String>,
    onClick: () -> Unit
) = with(sharedTransitionScope) {
    Card(
        modifier = modifier,
        onClick = onClick
    ) {
        Column {
            Icon(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "icon-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                imageVector = icon,
                contentDescription = null
            )
            Text(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "title-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                text = title
            )
            Text(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "description-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                text = description
            )
            pros.forEachIndexed { index, it ->
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "pro$index-$item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    text = it
                )
            }
            cons.forEachIndexed { index, it ->
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "con$index-$item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    text = it
                )
            }
            ButlerLargeSolidButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClick,
                enabled = true
            ) {
                Text("Select")
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AuthItemCompactFullscreen(
    modifier: Modifier = Modifier,
    item: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    icon: ImageVector,
    title: String,
    description: String,
    pros: List<String>,
    cons: List<String>,
    onNext: () -> Unit,
    onClose: () -> Unit
) = with(sharedTransitionScope) {
    Column(
        modifier = modifier
    ) {
        Icon(
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "icon-$item"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
            imageVector = icon,
            contentDescription = null
        )
        Text(
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "title-$item"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
            text = title
        )
        Text(
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "description-$item"),
                animatedVisibilityScope = animatedVisibilityScope
            ),
            text = description
        )
        pros.forEachIndexed { index, it ->
            Text(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "pro$index-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                text = it
            )
        }
        cons.forEachIndexed { index, it ->
            Text(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "con$index-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                text = it
            )
        }
        ButlerLargeOutlinedButton(
            onClick = onClose,
            enabled = true
        ) {
            Text("Close")
        }
        ButlerLargeSolidButton(
            onClick = onNext,
            enabled = true
        ) {
            Text("Select")
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AuthItemExpandedFullscreen(
    modifier: Modifier = Modifier,
    item: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    icon: ImageVector,
    title: String,
    description: String,
    pros: List<String>,
    cons: List<String>,
    onNext: () -> Unit,
    onClose: () -> Unit
) = with(sharedTransitionScope) {
    Row(
        modifier = modifier
    ) {
        Column {
            Icon(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "icon-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                imageVector = icon,
                contentDescription = null
            )
        }
        Column {
            Text(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "title-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                text = title
            )
            Text(
                modifier = Modifier.sharedElement(
                    rememberSharedContentState(key = "description-$item"),
                    animatedVisibilityScope = animatedVisibilityScope
                ),
                text = description
            )
            pros.forEachIndexed { index, it ->
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "pro$index-$item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    text = it
                )
            }
            cons.forEachIndexed { index, it ->
                Text(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "con$index-$item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    text = it
                )
            }
            Row {
                ButlerLargeOutlinedButton(
                    onClick = onClose,
                    enabled = true
                ) {
                    Text("Close")
                }
                ButlerLargeSolidButton(
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "select-$item"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                    onClick = onNext,
                    enabled = true
                ) {
                    Text("Select")
                }
            }
        }
    }
}
