package illyan.butler.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.navigation.NavController
import illyan.butler.getWindowSizeInDp
import illyan.butler.ui.components.ButlerDialogContentHolder
import illyan.butler.ui.components.ButlerDialogSurface
import illyan.butler.ui.components.dialogSize
import kotlinx.coroutines.delay

val LocalDialogDismissRequest = compositionLocalOf { {} }

@Composable
private fun DialogContent(
    modifier: Modifier,
    isDialogFullscreen: Boolean,
    isDialogClosing: Boolean,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
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
            content = content
        )
    }
}

@Composable
fun ButlerDialog(
    modifier: Modifier = Modifier,
    isDialogOpen: Boolean = true,
    isDialogFullscreen: Boolean = false,
    onDismissDialog: () -> Unit = {},
    onDialogClosed: () -> Unit = {},
    navController: NavController? = null,
    content: @Composable () -> Unit
) {
    var isDialogClosing by rememberSaveable { mutableStateOf(false) }
    var isDialogClosingAnimationEnded by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(isDialogOpen) {
        isDialogClosing = !isDialogOpen
        if (!isDialogOpen) delay(200)
        isDialogClosingAnimationEnded = !isDialogOpen
        isDialogClosing = false
    }

    if (!isDialogClosingAnimationEnded) {
        Dialog(
            onDismissRequest = { navController?.previousBackStackEntry?.let { navController.navigateUp() } ?: onDismissDialog() },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            ),
        ) {
            DialogContent(
                modifier,
                isDialogFullscreen,
                isDialogClosing,
                onDismissDialog,
                content
            )
        }
    } else {
        onDialogClosed()
    }
}
