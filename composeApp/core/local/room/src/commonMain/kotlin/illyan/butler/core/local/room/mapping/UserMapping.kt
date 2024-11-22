package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomUser
import illyan.butler.domain.model.DomainUser

fun RoomUser.toDomainModel() = DomainUser(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address?.toDomainModel(),
    accessToken = accessToken?.toDomainModel(),
    refreshToken = refreshToken?.toDomainModel()
)

fun DomainUser.toRoomModel() = RoomUser(
    id = id,
    email = email,
    username = username,
    displayName = displayName,
    phone = phone,
    fullName = fullName,
    photoUrl = photoUrl,
    address = address?.toRoomModel(),
    accessToken = accessToken?.toRoomModel(),
    refreshToken = refreshToken?.toRoomModel()
)
