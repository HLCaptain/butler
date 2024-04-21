package illyan.butler.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.ScreenTransition
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.components.ButlerDialogContentHolder
import illyan.butler.ui.components.ButlerDialogSurface
import illyan.butler.ui.components.dialogSize
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay

val LocalDialogDismissRequest = compositionLocalOf { {} }

@Composable
fun ButlerDialog(
    modifier: Modifier = Modifier,
    startScreens: List<Screen>,
    isDialogOpen: Boolean = true,
    isDialogFullscreen: Boolean = false,
    onDismissDialog: () -> Unit = {},
    onDialogClosed: () -> Unit = {}
) {
    var isDialogClosing by rememberSaveable { mutableStateOf(false) }
    var isDialogClosingAnimationEnded by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(isDialogOpen) {
        isDialogClosing = !isDialogOpen
        if (!isDialogOpen) delay(200)
        isDialogClosingAnimationEnded = !isDialogOpen
        isDialogClosing = false
    }

    var navigator by remember { mutableStateOf<Navigator?>(null) }
    val onDismissRequest: () -> Unit = {
        if (navigator?.lastItem == navigator?.items?.first()) {
            onDismissDialog()
        } else {
            navigator?.pop()
        }
    }
    var currentLastScreen by remember { mutableStateOf<Screen?>(null) }
    val dialogContent = @Composable {
        val containerSize = getWindowSizeInDp() // first: height, second: width
        val screenDimensionsDp by remember { derivedStateOf { containerSize.first to containerSize.second } }
        ButlerDialogContentHolder(
            modifier = modifier,
            surface = {
                val animatedRoundedCornerShape by animateDpAsState(
                    targetValue = if (isDialogFullscreen) 0.dp else 16.dp,
                    animationSpec = tween(200)
                )
                var isDialogVisible by rememberSaveable { mutableStateOf(false) }
                val sizeModifier = if (isDialogFullscreen) {
                    Modifier.animateContentSize().fillMaxSize()
                } else {
                    Modifier.animateContentSize(tween(0)).dialogSize(
                        screenDimensionsDp.second,
                        screenDimensionsDp.first,
                        minWidth = 0.dp,
                        minHeight = 0.dp
                    )
                }
                AnimatedVisibility(
                    visible = isDialogVisible,
                    enter = fadeIn(tween(200)) + scaleIn(tween(200), 0.8f),
                    exit = fadeOut(tween(100)) + scaleOut(tween(100), 0.8f),
                ) {
                    ButlerDialogSurface(shape = RoundedCornerShape(animatedRoundedCornerShape)) {
                        Box(
                            modifier = sizeModifier,
                            contentAlignment = Alignment.Center,
                            content = { it() }
                        )
                    }
                }
                LaunchedEffect(isDialogClosing) {
                    isDialogVisible = !isDialogClosing
                }
            }
        ) {
            CompositionLocalProvider(
                LocalDialogDismissRequest provides onDismissRequest,
            ) {
                Navigator(
                    screens = startScreens.ifEmpty { listOf(currentLastScreen!!) },
                ) { nav ->
                    // This hack is needed to avoid navigation issues with Voyager
                    // https://github.com/adrielcafe/voyager/issues/378
                    LaunchedEffect(startScreens) {
                        Napier.d("${nav.items}")
                        if (startScreens.isNotEmpty()) nav.replaceAll(startScreens)
                        navigator = nav
                        currentLastScreen = nav.lastItem
                    }
                    val animationTime = 200
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
                    ) { it.Content() }
                }
            }
        }
    }
    if (!isDialogClosingAnimationEnded) {
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
        ) {
            dialogContent()
        }
    } else {
        onDialogClosed()
    }
}
