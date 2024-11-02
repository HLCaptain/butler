package illyan.butler.ui.home

import illyan.butler.ui.components.GestureType

actual fun getNavBarTooltipGestures(): List<GestureType> {
    return listOf(GestureType.LongClick)
}