package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomAddress
import illyan.butler.domain.model.DomainAddress

fun DomainAddress.toRoomModel() = RoomAddress(
    street = street,
    city = city,
    state = state,
    zip = zip
)

fun RoomAddress.toDomainModel() = DomainAddress(
    street = street,
    city = city,
    state = state,
    zip = zip
)
