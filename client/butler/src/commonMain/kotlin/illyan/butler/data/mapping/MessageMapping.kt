package illyan.butler.data.mapping

import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.data.room.model.RoomMessage
import illyan.butler.db.Message
import illyan.butler.domain.model.DomainMessage

fun Message.toDomainModel() = DomainMessage(
    id = id,
    senderId = senderId,
    message = message,
    time = time,
    chatId = chatId,
    resourceIds = resourceIds,
)

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

fun MessageDto.toLocalModel() = Message(
    id = id!!,
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

fun Message.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainMessage.toLocalModel() = toNetworkModel().toLocalModel()
fun MessageDto.toDomainModel() = toLocalModel().toDomainModel()
fun RoomMessage.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainMessage.toRoomModel() = toNetworkModel().toRoomModel()