package illyan.butler.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

val ButlerExtraLargeShapeCornerDp = 24.dp
val ButlerLargeShapeCornerDp = 12.dp
val ButlerMediumShapeCornerDp = 8.dp
val ButlerSmallShapeCornerDp = 6.dp
val ButlerExtraSmallShapeCornerDp = 4.dp

@Composable
fun butlerShapes() = Shapes(
    extraLarge = RoundedCornerShape(ButlerExtraLargeShapeCornerDp),
    large = RoundedCornerShape(ButlerLargeShapeCornerDp),
    medium = RoundedCornerShape(ButlerMediumShapeCornerDp),
    small = RoundedCornerShape(ButlerSmallShapeCornerDp),
    extraSmall = RoundedCornerShape(ButlerExtraSmallShapeCornerDp),
)
