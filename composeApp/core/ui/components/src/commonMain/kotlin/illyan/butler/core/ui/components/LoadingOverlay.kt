package illyan.butler.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import illyan.butler.core.ui.utils.BackHandler

// Use LoadingScreenEffect, do not access composition locals directly
private val LocalLoadingScreenShow = staticCompositionLocalOf { {} }
private val LocalLoadingScreenHide = staticCompositionLocalOf { {} }

/**
 * This effect automatically sets the loading screen to be shown when
 * the key is true and hidden when the key is false.
 * Automatically hides the loading screen when the composable is disposed (when the loading screen was previously shown).
 */
@Composable
fun LoadingScreenEffect(key: Boolean) {
    val showLoadingScreen = LocalLoadingScreenShow.current
    val hideLoadingScreen = LocalLoadingScreenHide.current
    LaunchedEffect(key) {
        if (key) showLoadingScreen() else hideLoadingScreen()
    }
    DisposableEffect(Unit) {
        onDispose { if (key) hideLoadingScreen() }
    }
    BackHandler(enabled = key) { /* Do nothing while loading is shown */ }
}

@Composable
fun LoadingScreenOverlay(content: @Composable () -> Unit) {
    var numberOfLoadingScreens by rememberSaveable { mutableIntStateOf(0) }
    CompositionLocalProvider(
        LocalLoadingScreenShow provides { numberOfLoadingScreens++ },
        LocalLoadingScreenHide provides { numberOfLoadingScreens = (numberOfLoadingScreens - 1).coerceAtLeast(0) }
    ) {
        LoadingOverlay(
            isLoading = numberOfLoadingScreens > 0,
            content = content
        )
    }
}

@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    content: @Composable () -> Unit
) {
    Box {
        val blurRadius by animateFloatAsState(if (isLoading) 24f else 0f)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                .graphicsLayer {
                    renderEffect = BlurEffect(blurRadius, blurRadius)
                }
        ) {
            content()
        }
        if (isLoading) {
            Box(modifier = Modifier.pointerInput(Unit) { /* Catching clicks */ })
        }
        AnimatedVisibility(
            visible = isLoading,
            modifier = Modifier.fillMaxSize().align(Alignment.Center),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            LargeCircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}
