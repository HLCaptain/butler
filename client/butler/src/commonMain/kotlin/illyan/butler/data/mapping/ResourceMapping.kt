package illyan.butler.data.mapping

import illyan.butler.data.network.model.chat.ResourceDto
import illyan.butler.data.room.model.RoomResource
import illyan.butler.db.Resource
import illyan.butler.domain.model.DomainResource

fun Resource.toDomainModel() = DomainResource(
    id = id,
    type = type,
    data = data_,
)

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

fun ResourceDto.toLocalModel() = Resource(
    id = id!!,
    type = type,
    data_ = data,
)

fun ResourceDto.toRoomModel() = RoomResource(
    id = id!!,
    mimeType = type,
    data = data,
)

fun Resource.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainResource.toLocalModel() = toNetworkModel().toLocalModel()
fun ResourceDto.toDomainModel() = toLocalModel().toDomainModel()
fun RoomResource.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainResource.toRoomModel() = toNetworkModel().toRoomModel()