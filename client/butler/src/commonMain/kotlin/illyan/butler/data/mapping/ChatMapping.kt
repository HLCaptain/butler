package illyan.butler.data.mapping

import illyan.butler.data.network.model.chat.ChatDto
import illyan.butler.data.room.model.RoomChat
import illyan.butler.domain.model.DomainChat

fun RoomChat.toDomainModel() = DomainChat(
    id = id,
    created = created,
    name = name,
    members = members,
    aiEndpoints = aiEndpoints,
    summary = summary
)

fun DomainChat.toNetworkModel() = ChatDto(
    id = id,
    created = created,
    name = name,
    members = members,
    aiEndpoints = aiEndpoints,
    summary = summary
)

fun ChatDto.toRoomModel() = RoomChat(
    id = id!!,
    created = created!!,
    name = name,
    members = members,
    aiEndpoints = aiEndpoints,
    summary = summary
)

fun ChatDto.toDomainModel() = toRoomModel().toDomainModel()
fun RoomChat.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainChat.toRoomModel() = toNetworkModel().toRoomModel()
