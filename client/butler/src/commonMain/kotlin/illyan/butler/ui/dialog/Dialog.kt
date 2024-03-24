package illyan.butler.ui.dialog

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.ScreenTransition
import illyan.butler.ui.components.ButlerDialogContentHolder
import illyan.butler.ui.components.ButlerDialogSurface

val LocalDialogDismissRequest = compositionLocalOf { {} }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButlerDialog(
    startScreen: Screen,
    isDialogOpen: Boolean = true,
    onDialogClosed: () -> Unit = {},
) {
    if (isDialogOpen) {
        // Don't use exit animations because
        // it looks choppy while Dialog resizes due to content change.v
        val navigator = LocalNavigator.currentOrThrow
        val onDismissRequest: () -> Unit = {
            val currentScreen = navigator.lastItem
            val firstScreen = navigator.items.first()
            if (currentScreen == firstScreen) {
                onDialogClosed()
            } else {
                navigator.pop()
            }
        }
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = onDismissRequest,
        ) {
            ButlerDialogContentHolder(
                surface = {
                    ButlerDialogSurface(
//                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Box(
//                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                            content = { it() }
                        )
                    }
                }
            ) {
                CompositionLocalProvider(
                    LocalDialogDismissRequest provides onDismissRequest,
                ) {
                    Navigator(startScreen) {
                        ScreenTransition(
                            navigator = LocalNavigator.currentOrThrow,
                            transition = {
                                (slideInHorizontally(tween(200)) { it / 8 } + fadeIn(tween(200))) togetherWith
                                        (slideOutHorizontally(tween(200)) { -it / 8 } + fadeOut(tween(200)))
                            }
                        ) {
                            it.Content()
                        }
                    }
                }
            }
        }
    }
}
