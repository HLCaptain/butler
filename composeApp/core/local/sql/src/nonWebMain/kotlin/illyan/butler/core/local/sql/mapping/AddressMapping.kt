package illyan.butler.core.local.sql.mapping

import illyan.butler.core.local.sql.model.RoomAddress
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
