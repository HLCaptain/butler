package illyan.butler.data.mapping

import illyan.butler.data.network.model.chat.ResourceDto
import illyan.butler.data.local.room.model.RoomResource
import illyan.butler.model.DomainResource

fun RoomResource.toDomainModel() = DomainResource(
    id = id,
    type = mimeType,
    data = data,
)

fun DomainResource.toNetworkModel() = ResourceDto(
    id = id,
    type = type,
    data = data,
)

fun ResourceDto.toRoomModel() = RoomResource(
    id = id!!,
    mimeType = type,
    data = data,
)

fun ResourceDto.toDomainModel() = toRoomModel().toDomainModel()
fun RoomResource.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainResource.toRoomModel() = toNetworkModel().toRoomModel()
