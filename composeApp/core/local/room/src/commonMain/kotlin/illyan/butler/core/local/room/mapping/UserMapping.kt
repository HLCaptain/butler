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
    filters = filterOptions,
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
    filterOptions = filters,
)
