package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomPreferences
import illyan.butler.domain.model.DomainPreferences

fun DomainPreferences.toRoomModel() = RoomPreferences(
    userId = userId,
    analyticsEnabled = analyticsEnabled,
    dynamicColorEnabled = dynamicColorEnabled,
    theme = theme
)

fun RoomPreferences.toDomainModel() = DomainPreferences(
    userId = userId,
    analyticsEnabled = analyticsEnabled,
    dynamicColorEnabled = dynamicColorEnabled,
    theme = theme
)
