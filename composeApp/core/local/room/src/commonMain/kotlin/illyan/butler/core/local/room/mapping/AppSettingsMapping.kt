package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomAppSettings
import illyan.butler.domain.model.AppSettings

fun RoomAppSettings.toDomainModel() = AppSettings(
    clientId = clientId,
    preferences = preferences.toDomainModel(),
    hostUrl = hostUrl
)

fun AppSettings.toRoomModel(
    firstSignInHappenedYet: Boolean = false,
    isTutorialDone: Boolean = false
) = RoomAppSettings(
    clientId = clientId,
    preferences = preferences.toRoomModel(),
    firstSignInHappenedYet = firstSignInHappenedYet,
    isTutorialDone = isTutorialDone,
    hostUrl = hostUrl
)
