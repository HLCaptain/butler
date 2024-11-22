package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomMessage
import illyan.butler.domain.model.DomainMessage

fun RoomMessage.toDomainModel() = DomainMessage(
    id = id,
    senderId = senderId,
    message = message,
    time = time,
    chatId = chatId,
    resourceIds = resourceIds,
)

fun DomainMessage.toRoomModel() = RoomMessage(
    id = id!!,
    senderId = senderId,
    message = message,
    time = time!!,
    chatId = chatId,
    resourceIds = resourceIds,
)
