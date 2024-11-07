package illyan.butler.data.settings

import illyan.butler.core.local.room.mapping.toDomainModel
import illyan.butler.core.local.room.mapping.toRoomModel
import illyan.butler.core.local.room.model.RoomAppSettings
import illyan.butler.domain.model.AppSettings

fun RoomAppSettings.toDomainModel() = AppSettings(
    clientId = clientId,
    preferences = preferences.toDomainModel()
)

fun AppSettings.toRoomModel(
    firstSignInHappenedYet: Boolean = false,
    isTutorialDone: Boolean = false,
    hostUrl: String = "",
) = RoomAppSettings(
    clientId = clientId,
    preferences = preferences.toRoomModel(),
    firstSignInHappenedYet = firstSignInHappenedYet,
    isTutorialDone = isTutorialDone,
    hostUrl = hostUrl
)
