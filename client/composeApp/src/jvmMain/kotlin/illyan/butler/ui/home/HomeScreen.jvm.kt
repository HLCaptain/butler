package illyan.butler.ui.home

import illyan.butler.ui.components.GestureType
import kotlin.time.Duration.Companion.seconds

actual fun getNavBarTooltipGestures(): List<GestureType> {
    return listOf(GestureType.Hover(1.seconds))
}