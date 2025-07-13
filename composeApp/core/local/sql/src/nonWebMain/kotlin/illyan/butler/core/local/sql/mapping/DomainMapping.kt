package illyan.butler.core.local.sql.mapping

import illyan.butler.core.local.sql.model.RoomPreferences
import illyan.butler.domain.model.Preferences

fun Preferences.toRoomModel() = RoomPreferences(
    userId = userId,
    analyticsEnabled = analyticsEnabled,
    theming = theming,
)

fun RoomPreferences.toDomainModel() = Preferences(
    userId = userId,
    analyticsEnabled = analyticsEnabled,
    theming = theming,
)
