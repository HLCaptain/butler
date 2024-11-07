package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomResource
import illyan.butler.domain.model.DomainResource

fun RoomResource.toDomainModel() = DomainResource(
    id = id,
    type = mimeType,
    data = data,
)

fun DomainResource.toRoomModel() = RoomResource(
    id = id!!,
    mimeType = type,
    data = data,
)
