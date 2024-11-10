package illyan.butler.core.ui

import illyan.butler.ui.components.GestureType

actual fun getTooltipGestures(): List<GestureType> {
    return listOf(GestureType.LongClick)
}