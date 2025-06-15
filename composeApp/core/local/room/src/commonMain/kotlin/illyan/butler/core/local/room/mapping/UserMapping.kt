@file:OptIn(ExperimentalUuidApi::class)

package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomUser
import illyan.butler.domain.model.User
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun RoomUser.toDomainModel() = User(
    id = Uuid.parse(id),
    endpoint = endpoint,
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

fun User.toRoomModel() = RoomUser(
    id = id.toString(),
    endpoint = endpoint,
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
