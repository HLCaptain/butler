package illyan.butler.ui.dialog

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import illyan.butler.ui.components.ButlerDialogContentHolder
import illyan.butler.ui.components.ButlerDialogSurface
import illyan.butler.ui.components.dialogSize
import io.github.aakira.napier.Napier

val LocalDialogDismissRequest = compositionLocalOf { {} }

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ButlerDialog(
    getStartScreen: () -> Screen,
    isDialogOpen: Boolean = true,
    isDialogFullscreen: Boolean = false,
    onDialogClosed: () -> Unit = {}
) {
    if (isDialogOpen) {
        // Don't use exit animations because
        // it looks choppy while Dialog resizes due to content change.v
        lateinit var navigator: Navigator
        val onDismissRequest: () -> Unit = {
            val currentScreen = navigator.items.last()
            val firstScreen = navigator.items.first()
//            Napier.d("currentScreen: $currentScreen, firstScreen: $firstScreen")
            Napier.d("navigator.items: ${navigator.items}")

            if (currentScreen == firstScreen) {
                onDialogClosed()
            } else {
                navigator.pop()
            }
        }
//        Box(propagateMinConstraints = true) {}
        val configuration = LocalWindowInfo.current
        val density = LocalDensity.current
        val screenDimensionsDp by remember { derivedStateOf { configuration.containerSize.width.dp / density.density to configuration.containerSize.height.dp / density.density } }
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
        ) {
            ButlerDialogContentHolder(
//                modifier = Modifier.animateContentSize(),
                surface = {
                    val animatedRoundedCornerShape by animateDpAsState(
                        targetValue = if (isDialogFullscreen) 0.dp else 16.dp,
                        animationSpec = tween(200)
                    )
                    val sizeModifier = if (isDialogFullscreen) {
                        Modifier.fillMaxSize()
                    } else {
                        Modifier.dialogSize(screenDimensionsDp.first, screenDimensionsDp.second)
                    }
                    ButlerDialogSurface(
                        modifier = Modifier.animateContentSize(),
                        shape = RoundedCornerShape(animatedRoundedCornerShape)
                    ) {
                        Box(
                            modifier = sizeModifier,
                            contentAlignment = Alignment.Center,
                            content = { it() }
                        )
                    }
                }
            ) {
                CompositionLocalProvider(
                    LocalDialogDismissRequest provides onDismissRequest,
                ) {
                    val startScreen by remember {
                        derivedStateOf(getStartScreen)
                    }
                    Navigator(
                        screen = startScreen
                    ) { nav ->
                        // This hack is needed to avoid navigation issues with Voyager
                        // https://github.com/adrielcafe/voyager/issues/378
                        LaunchedEffect(startScreen) {
                            Napier.d("${nav.items}")
                            nav.replaceAll(startScreen)
                        }
                        val animationTime = 200
                        navigator = nav
                        ScreenTransition(
                            navigator = nav,
                            enterTransition = {
                                (slideInHorizontally(tween(animationTime)) { it / 8 } + fadeIn(tween(animationTime))) togetherWith
                                        (slideOutHorizontally(tween(animationTime)) { -it / 8 } + fadeOut(tween(animationTime)))
                            },
                            exitTransition = {
                                (slideInHorizontally(tween(animationTime)) { -it / 8 } + fadeIn(tween(animationTime))) togetherWith
                                        (slideOutHorizontally(tween(animationTime)) { it / 8 } + fadeOut(tween(animationTime)))
                            }
                        ) {
                            it.Content()
                            LaunchedEffect(it) {
                                Napier.d("Navigated to: $it")
                            }
                        }
                    }
                }
            }
        }
    }
}
