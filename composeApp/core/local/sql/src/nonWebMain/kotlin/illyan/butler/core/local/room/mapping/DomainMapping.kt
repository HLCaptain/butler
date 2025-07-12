package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomPreferences
import illyan.butler.domain.model.Preferences

fun Preferences.toRoomModel() = RoomPreferences(
    userId = userId,
    analyticsEnabled = analyticsEnabled,
    dynamicColorEnabled = dynamicColorEnabled,
    theme = theme
)

fun RoomPreferences.toDomainModel() = Preferences(
    userId = userId,
    analyticsEnabled = analyticsEnabled,
    dynamicColorEnabled = dynamicColorEnabled,
    theme = theme
)
