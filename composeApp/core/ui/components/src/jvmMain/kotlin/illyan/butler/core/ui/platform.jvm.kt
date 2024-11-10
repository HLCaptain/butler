package illyan.butler.core.ui

import illyan.butler.ui.components.GestureType
import kotlin.time.Duration.Companion.seconds

actual fun getTooltipGestures(): List<GestureType> {
    return listOf(GestureType.Hover(1.seconds))
}