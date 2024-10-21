package illyan.butler.data.mapping

import illyan.butler.data.local.room.model.RoomAppSettings
import illyan.butler.model.AppSettings

fun RoomAppSettings.toDomainModel() = AppSettings(
    clientId = clientId,
    preferences = preferences
)

fun AppSettings.toRoomModel(
    firstSignInHappenedYet: Boolean = false,
    isTutorialDone: Boolean = false,
    hostUrl: String = "",
) = RoomAppSettings(
    clientId = clientId,
    preferences = preferences,
    firstSignInHappenedYet = firstSignInHappenedYet,
    isTutorialDone = isTutorialDone,
    hostUrl = hostUrl
)
