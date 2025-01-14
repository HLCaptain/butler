package illyan.butler.core.ui.utils

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector4D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity

/**
 * Percentage based [CornerSize] is not supported, unless a [size] is provided.
 */
@Composable
fun animateCornerBasedShapeAsState(
    targetValue: CornerBasedShape,
    size: Size = Size.Unspecified,
    animationSpec: AnimationSpec<CornerBasedShape> = tween()
): State<CornerBasedShape> {
    val density = LocalDensity.current
    val classicTypeConverter = object : TwoWayConverter<CornerBasedShape, AnimationVector4D> {
        override val convertFromVector: (AnimationVector4D) -> CornerBasedShape
            get() = {
                when (targetValue) {
                    is RoundedCornerShape -> RoundedCornerShape(
                        topStart = it.v1,
                        topEnd = it.v2,
                        bottomStart = it.v3,
                        bottomEnd = it.v4
                    )

                    is CutCornerShape -> CutCornerShape(
                        topStart = it.v1,
                        topEnd = it.v2,
                        bottomStart = it.v3,
                        bottomEnd = it.v4
                    )

                    else -> throw IllegalArgumentException("Unsupported shape")
                }
            }
        override val convertToVector: (CornerBasedShape) -> AnimationVector4D
            get() = {
                listOf(
                    it.topStart.toPx(density = density, shapeSize = size),
                    it.topEnd.toPx(density = density, shapeSize = size),
                    it.bottomStart.toPx(density = density, shapeSize = size),
                    it.bottomEnd.toPx(density = density, shapeSize = size)
                ).filterNot { corner ->
                    if (corner.isNaN()) throw IllegalArgumentException("Corner size in Px cannot be NaN. Happens when the CornerBasedShape has PercentCornerSize and the size is not provided.")
                    corner.isNaN()
                }.let { corners ->
                    AnimationVector4D(corners[0], corners[1], corners[2], corners[3])
                }
            }
    }
    return animateValueAsState(
        targetValue = targetValue,
        animationSpec = animationSpec,
        typeConverter = classicTypeConverter
    )
}