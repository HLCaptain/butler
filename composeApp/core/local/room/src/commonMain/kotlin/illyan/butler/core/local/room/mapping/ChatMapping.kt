package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomChat
import illyan.butler.domain.model.DomainChat

fun RoomChat.toDomainModel() = DomainChat(
    id = id,
    created = created,
    name = name,
    members = members,
    aiEndpoints = aiEndpoints,
    summary = summary
)

fun DomainChat.toRoomModel() = RoomChat(
    id = id!!,
    created = created!!,
    name = name,
    members = members,
    aiEndpoints = aiEndpoints,
    summary = summary
)
