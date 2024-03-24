package illyan.butler.ui.dialog

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.transitions.ScreenTransition
import illyan.butler.ui.components.ButlerDialogContentHolder
import illyan.butler.ui.components.ButlerDialogSurface
import io.github.aakira.napier.Napier

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
        val currentNavigator = LocalNavigator.currentOrThrow
        var navigator by remember { mutableStateOf(currentNavigator) }
        val onDismissRequest: () -> Unit = {
            val currentScreen = navigator.items.last()
            val firstScreen = navigator.items.first()
            Napier.d("currentScreen: $currentScreen, firstScreen: $firstScreen")
            if (currentScreen == firstScreen) {
                onDialogClosed()
            } else {
                navigator.pop()
            }
        }
//        Box(propagateMinConstraints = true) {}
        Dialog(
            onDismissRequest = onDismissRequest,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = false
            ),
        ) {
            ButlerDialogContentHolder(
                surface = {
                    ButlerDialogSurface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                            content = { it() }
                        )
                    }
                }
            ) {
                CompositionLocalProvider(
                    LocalDialogDismissRequest provides onDismissRequest,
                ) {
                    Navigator(startScreen) { nav ->
                        navigator = nav
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
