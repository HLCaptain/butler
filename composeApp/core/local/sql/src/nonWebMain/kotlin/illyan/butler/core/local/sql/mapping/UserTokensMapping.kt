package illyan.butler.core.local.sql.mapping

import illyan.butler.core.local.sql.model.RoomUserTokens
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
