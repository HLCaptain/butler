package illyan.butler.data.mapping

import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.data.room.model.RoomMessage
import illyan.butler.domain.model.DomainMessage

fun RoomMessage.toDomainModel() = DomainMessage(
    id = id,
    senderId = senderId,
    message = message,
    time = time,
    chatId = chatId,
    resourceIds = resourceIds,
)

fun DomainMessage.toNetworkModel() = MessageDto(
    id = id,
    senderId = senderId,
    message = message,
    time = time,
    chatId = chatId,
    resourceIds = resourceIds,
)

fun MessageDto.toRoomModel() = RoomMessage(
    id = id!!,
    senderId = senderId,
    message = message,
    time = time!!,
    chatId = chatId,
    resourceIds = resourceIds,
)

fun RoomMessage.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainMessage.toRoomModel() = toNetworkModel().toRoomModel()
fun MessageDto.toDomainModel() = toRoomModel().toDomainModel()
