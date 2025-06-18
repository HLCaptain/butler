package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomAddress
import illyan.butler.domain.model.Address

fun Address.toRoomModel() = RoomAddress(
    street = street,
    city = city,
    state = state,
    zip = zip
)

fun RoomAddress.toDomainModel() = Address(
    street = street,
    city = city,
    state = state,
    zip = zip
)
