package illyan.butler.core.ui.utils

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Composable
operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) + other.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + other.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection) + other.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + other.calculateBottomPadding()
    )
}

@Composable
operator fun PaddingValues.minus(other: PaddingValues): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = calculateStartPadding(layoutDirection) - other.calculateStartPadding(layoutDirection),
        top = calculateTopPadding() - other.calculateTopPadding(),
        end = calculateEndPadding(layoutDirection) - other.calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() - other.calculateBottomPadding()
    )
}

@Composable
fun animatePaddingValuesAsState(
    targetValue: PaddingValues,
    animationSpec: AnimationSpec<PaddingValues> = tween()
): State<PaddingValues> {
    return animateValueAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        typeConverter = object: TwoWayConverter<PaddingValues, AnimationVector4D> {
        override val convertFromVector: (AnimationVector4D) -> PaddingValues
            get() = { PaddingValues(it.v1.dp, it.v2.dp, it.v3.dp, it.v4.dp) }
        override val convertToVector: (PaddingValues) -> AnimationVector4D
            get() = { AnimationVector4D(it.calculateStartPadding(LayoutDirection.Ltr).value, it.calculateTopPadding().value, it.calculateEndPadding(LayoutDirection.Ltr).value, it.calculateBottomPadding().value) }
    })
}
