package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomUserTokens
import illyan.butler.domain.model.UserTokens

fun RoomUserTokens.toDomainModel() = UserTokens(
    accessToken = accessToken?.toDomainModel(),
    refreshToken = refreshToken?.toDomainModel()
)

fun UserTokens.toRoomModel(userId: String) = RoomUserTokens(
    userId = userId,
    accessToken = accessToken?.toRoomModel(),
    refreshToken = refreshToken?.toRoomModel()
)
