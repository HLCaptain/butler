package illyan.butler.core.ui.components

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import illyan.butler.core.ui.utils.getWindowSizeInDp
import kotlinx.coroutines.delay

@Composable
private fun DialogContent(
    modifier: Modifier,
    isDialogFullscreen: Boolean,
    isDialogClosing: Boolean,
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
                Modifier.dialogSize(
                    screenDimensionsDp.second,
                    screenDimensionsDp.first,
                    minWidth = 0.dp,
                    minHeight = 0.dp
                )
            }
            AnimatedVisibility(
                visible = isDialogVisible,
                enter = fadeIn(tween(200)) + scaleIn(tween(200), if (isDialogFullscreen) 0.4f else 0.8f),
                exit = fadeOut(tween(100)) + scaleOut(tween(100), if (isDialogFullscreen) 0.4f else 0.8f),
            ) {
                ButlerDialogSurface(
                    shape = RoundedCornerShape(animatedRoundedCornerShape),
                    color = MaterialTheme.colorScheme.surface,
                ) {
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
        },
        content = content
    )
}

@Composable
fun ButlerDialog(
    modifier: Modifier = Modifier,
    isDialogOpen: Boolean = true,
    isDialogFullscreen: Boolean = false,
    onDismissDialog: () -> Unit = {},
    onDialogClosed: () -> Unit = {},
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
            onDismissRequest = onDismissDialog,
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
                content
            )
        }
    } else {
        onDialogClosed()
    }
}
