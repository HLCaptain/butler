package illyan.butler.ui.components

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.constrain
import androidx.compose.ui.unit.constrainHeight
import androidx.compose.ui.unit.constrainWidth
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

@Composable
fun ButlerTwoPane(
    modifier: Modifier = Modifier,
    strategy: TwoPaneStrategy,
    first: @Composable () -> Unit,
    second: @Composable () -> Unit
) {
    val density = LocalDensity.current
    Layout(
        modifier = modifier.wrapContentSize(),
        content = {
            Box(Modifier.layoutId("list")) {
                first()
            }
            Box(Modifier.layoutId("detail")) {
                second()
            }
        }
    ) { measurable, constraints ->
        val firstMeasurable = measurable.find { it.layoutId == "list" }!!
        val secondMeasurable = measurable.find { it.layoutId == "detail" }!!

        layout(constraints.maxWidth, constraints.maxHeight) {
            val splitResult = strategy.calculateSplitResult(
                density = density,
                layoutDirection = layoutDirection,
                layoutCoordinates = coordinates ?: return@layout
            )

            val gapOrientation = splitResult.gapOrientation
            val gapBounds = splitResult.gapBounds

            val gapLeft = constraints.constrainWidth(gapBounds.left.roundToInt())
            val gapRight = constraints.constrainWidth(gapBounds.right.roundToInt())
            val gapTop = constraints.constrainHeight(gapBounds.top.roundToInt())
            val gapBottom = constraints.constrainHeight(gapBounds.bottom.roundToInt())
            val firstConstraints =
                if (gapOrientation == Orientation.Vertical) {
                    val width = when (layoutDirection) {
                        LayoutDirection.Ltr -> gapLeft
                        LayoutDirection.Rtl -> constraints.maxWidth - gapRight
                    }

                    constraints.copy(minWidth = width, maxWidth = width)
                } else {
                    constraints.copy(minHeight = gapTop, maxHeight = gapTop)
                }
            val secondConstraints =
                if (gapOrientation == Orientation.Vertical) {
                    val width = when (layoutDirection) {
                        LayoutDirection.Ltr -> constraints.maxWidth - gapRight
                        LayoutDirection.Rtl -> gapLeft
                    }
                    constraints.copy(minWidth = width, maxWidth = width)
                } else {
                    val height = constraints.maxHeight - gapBottom
                    constraints.copy(minHeight = height, maxHeight = height)
                }
            val firstPlaceable = firstMeasurable.measure(constraints.constrain(firstConstraints))
            val secondPlaceable = secondMeasurable.measure(constraints.constrain(secondConstraints))

            firstPlaceable.placeRelative(0, 0)
            val detailOffsetX =
                if (gapOrientation == Orientation.Vertical) {
                    constraints.maxWidth - secondPlaceable.width
                } else {
                    0
                }
            val detailOffsetY =
                if (gapOrientation == Orientation.Vertical) {
                    0
                } else {
                    constraints.maxHeight - secondPlaceable.height
                }
            secondPlaceable.placeRelative(detailOffsetX, detailOffsetY)
        }
    }
}

fun interface TwoPaneStrategy {
    /**
     * Calculates the split result in local bounds of the [TwoPane].
     *
     * @param density the [Density] for measuring and laying out
     * @param layoutDirection the [LayoutDirection] for measuring and laying out
     * @param layoutCoordinates the [LayoutCoordinates] of the [TwoPane]
     */
    fun calculateSplitResult(
        density: Density,
        layoutDirection: LayoutDirection,
        layoutCoordinates: LayoutCoordinates
    ): SplitResult
}

data class SplitResult(

    /**
     * Whether the gap is vertical or horizontal
     */
    val gapOrientation: Orientation,

    /**
     * The bounds that are nether a `start` pane or an `end` pane, but a separation between those
     * two. In case width or height is 0 - it means that the gap itself is a 0 width/height, but the
     * place within the layout is still defined.
     *
     * The [gapBounds] should be defined in local bounds to the [TwoPane].
     */
    val gapBounds: Rect
)

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * The gap will be placed at the given [splitFraction] from start, with the given [gapWidth].
 *
 * This strategy is _not_ fold aware.
 */
fun FractionHorizontalTwoPaneStrategy(
    splitFraction: Float,
    gapWidth: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, layoutDirection, layoutCoordinates ->
    val splitX = layoutCoordinates.size.width * when (layoutDirection) {
        LayoutDirection.Ltr -> splitFraction
        LayoutDirection.Rtl -> 1 - splitFraction
    }
    val splitWidthPixel = with(density) { gapWidth.toPx() }

    SplitResult(
        gapOrientation = Orientation.Vertical,
        gapBounds = Rect(
            left = splitX - splitWidthPixel / 2f,
            top = 0f,
            right = splitX + splitWidthPixel / 2f,
            bottom = layoutCoordinates.size.height.toFloat(),
        )
    )
}

fun FractionReversedHorizontalTwoPaneStrategy(
    splitFraction: Float,
    gapWidth: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, layoutDirection, layoutCoordinates ->
    val splitX = layoutCoordinates.size.width * when (layoutDirection) {
        LayoutDirection.Ltr -> 1 - splitFraction
        LayoutDirection.Rtl -> splitFraction
    }
    val splitWidthPixel = with(density) { gapWidth.toPx() }

    SplitResult(
        gapOrientation = Orientation.Vertical,
        gapBounds = Rect(
            left = splitX - splitWidthPixel / 2f,
            top = 0f,
            right = splitX + splitWidthPixel / 2f,
            bottom = layoutCoordinates.size.height.toFloat(),
        )
    )
}

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * The gap will be placed at [splitOffset] either from the start or end based on
 * [offsetFromStart], with the given [gapWidth].
 *
 * This strategy is _not_ fold aware.
 */
fun FixedOffsetHorizontalTwoPaneStrategy(
    splitOffset: Dp,
    offsetFromStart: Boolean,
    gapWidth: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, layoutDirection, layoutCoordinates ->
        val splitOffsetPixel = with(density) { splitOffset.toPx() }
        val splitX = when (layoutDirection) {
            LayoutDirection.Ltr ->
                if (offsetFromStart) {
                    splitOffsetPixel
                } else {
                    layoutCoordinates.size.width - splitOffsetPixel
                }

            LayoutDirection.Rtl ->
                if (offsetFromStart) {
                    layoutCoordinates.size.width - splitOffsetPixel
                } else {
                    splitOffsetPixel
                }
        }
        val splitWidthPixel = with(density) { gapWidth.toPx() }

        SplitResult(
            gapOrientation = Orientation.Vertical,
            gapBounds = Rect(
                left = splitX - splitWidthPixel / 2f,
                top = 0f,
                right = splitX + splitWidthPixel / 2f,
                bottom = layoutCoordinates.size.height.toFloat(),
            )
        )
    }

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * The split will be placed at the given [splitFraction] from start, with the given [gapHeight].
 *
 * This strategy is _not_ fold aware.
 */
fun FractionVerticalTwoPaneStrategy(
    splitFraction: Float,
    gapHeight: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, _, layoutCoordinates ->
    val splitY = layoutCoordinates.size.height * splitFraction
    val splitHeightPixel = with(density) { gapHeight.toPx() }

    SplitResult(
        gapOrientation = Orientation.Horizontal,
        gapBounds = Rect(
            left = 0f,
            top = splitY - splitHeightPixel / 2f,
            right = layoutCoordinates.size.width.toFloat(),
            bottom = splitY + splitHeightPixel / 2f,
        )
    )
}

fun FractionReversedVerticalTwoPaneStrategy(
    splitFraction: Float,
    gapHeight: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, _, layoutCoordinates ->
    val splitY = layoutCoordinates.size.height * (1 - splitFraction)
    val splitHeightPixel = with(density) { gapHeight.toPx() }

    SplitResult(
        gapOrientation = Orientation.Horizontal,
        gapBounds = Rect(
            left = 0f,
            top = splitY - splitHeightPixel / 2f,
            right = layoutCoordinates.size.width.toFloat(),
            bottom = splitY + splitHeightPixel / 2f,
        )
    )
}

/**
 * Returns a [TwoPaneStrategy] that will place the slots horizontally.
 *
 * The split will be placed at [splitOffset] either from the top or bottom based on
 * [offsetFromTop], with the given [gapHeight].
 *
 * This strategy is _not_ fold aware.
 */
fun FixedOffsetVerticalTwoPaneStrategy(
    splitOffset: Dp,
    offsetFromTop: Boolean,
    gapHeight: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, _, layoutCoordinates ->
    val splitOffsetPixel = with(density) { splitOffset.toPx() }
    val splitY =
        if (offsetFromTop) {
            splitOffsetPixel
        } else {
            layoutCoordinates.size.height - splitOffsetPixel
        }
    val splitHeightPixel = with(density) { gapHeight.toPx() }

    SplitResult(
        gapOrientation = Orientation.Horizontal,
        gapBounds = Rect(
            left = 0f,
            top = splitY - splitHeightPixel / 2f,
            right = layoutCoordinates.size.width.toFloat(),
            bottom = splitY + splitHeightPixel / 2f,
        )
    )
}

fun FixedOffsetReversedVerticalTwoPaneStrategy(
    splitOffset: Dp,
    offsetFromTop: Boolean,
    gapHeight: Dp = 0.dp,
): TwoPaneStrategy = TwoPaneStrategy { density, _, layoutCoordinates ->
    val splitOffsetPixel = with(density) { splitOffset.toPx() }
    val splitY =
        if (offsetFromTop) {
            layoutCoordinates.size.height - splitOffsetPixel
        } else {
            splitOffsetPixel
        }
    val splitHeightPixel = with(density) { gapHeight.toPx() }

    SplitResult(
        gapOrientation = Orientation.Horizontal,
        gapBounds = Rect(
            left = 0f,
            top = splitY - splitHeightPixel / 2f,
            right = layoutCoordinates.size.width.toFloat(),
            bottom = splitY + splitHeightPixel / 2f,
        )
    )
}
