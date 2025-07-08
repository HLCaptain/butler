package illyan.butler.core.ui

import illyan.butler.core.ui.components.GestureType

actual fun getTooltipGestures(): List<GestureType> {
    return listOf(GestureType.LongClick)
}