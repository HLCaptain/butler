package illyan.butler.core.ui.components

import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.EaseOutQuart
import androidx.compose.animation.core.TweenSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.back
import illyan.butler.generated.resources.next
import org.jetbrains.compose.resources.stringResource

@Composable
fun ButlerProgressBar(
    modifier: Modifier = Modifier,
    onNext: () -> Unit,
    onBack: () -> Unit,
    canGoBack: Boolean = true,
    canGoForward: Boolean = true,
    currentStep: Int = 1,
    numberOfSteps: Int = 5
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row {
            ButlerSmallSolidButton(
                onClick = onBack,
                enabled = canGoBack,
            ) {
                Text(stringResource(Res.string.back))
            }
            Spacer(Modifier.weight(1f))
            ButlerSmallSolidButton(
                onClick = onNext,
                enabled = canGoForward,
            ) {
                Text(stringResource(Res.string.next))
            }
        }

        var layoutWidth by remember { mutableStateOf(0f) }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layoutWidth = placeable.width.toFloat()
                    layout(placeable.width, placeable.height) {
                        placeable.place(0, 0)
                    }
                },
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.weight((1000 / layoutWidth - 1f).coerceAtLeast(0.1f)))
            Row(modifier = Modifier.padding(16.dp).weight(1f)) {
                for (i in 1..numberOfSteps) {
                    val animatedProgress by animateFloatAsState(
                        // Workaround for faster animation for previous values
                        // Progress is in 0..1, so negative values are OK
                        targetValue = if (i == currentStep) 1f else if (i < currentStep) 1.01f else -0.01f,
                        animationSpec = if (i == currentStep) ButlerProgressBarDefaults.ProgressBarFillAnimation else ButlerProgressBarDefaults.ProgressBarQuickFillAnimation,
                        label = "Current Step state"
                    )
                    LinearProgressIndicator(
                        modifier = Modifier.height(ButlerProgressBarDefaults.ProgressLineThickness).weight(1f, fill = false),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = ButlerProgressBarDefaults.DisabledColor,
                        progress = { animatedProgress },
                        gapSize = -ButlerProgressBarDefaults.ProgressLineThickness,
                        drawStopIndicator = {}
                    )
                    if (i != numberOfSteps) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
            Spacer(Modifier.weight((1000 / layoutWidth - 1f).coerceAtLeast(0.1f)))
        }
    }
}

object ButlerProgressBarDefaults {
    val ProgressLineThickness = 6.dp
    val ProgressBarAnimationDuration = 1000
    val ProgressBarQuickAnimationDuration = 500
    val ProgressBarFillAnimation: TweenSpec<Float> = tween(durationMillis = ProgressBarAnimationDuration, easing = EaseInOutQuart)
    val ProgressBarQuickFillAnimation: TweenSpec<Float> = tween(durationMillis = ProgressBarQuickAnimationDuration, easing = EaseOutQuart)
    val DisabledColor: Color @Composable get() = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
}