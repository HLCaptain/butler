package illyan.butler.data.mapping

import illyan.butler.data.network.model.chat.MessageDto
import illyan.butler.db.Message
import illyan.butler.domain.model.DomainMessage

fun Message.toDomainModel() = DomainMessage(
    id = id,
    senderUUID = senderId,
    role = role,
    message = message,
    timestamp = timestamp,
    chatId = chatId
)

fun DomainMessage.toNetworkModel() = MessageDto(
    id = id,
    senderId = senderUUID,
    role = role,
    message = message,
    timestamp = timestamp,
    chatId = chatId
)

fun MessageDto.toLocalModel() = Message(
    id = id!!,
    senderId = senderId,
    role = role,
    message = message,
    timestamp = timestamp,
    chatId = chatId
)

fun Message.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainMessage.toLocalModel() = toNetworkModel().toLocalModel()
fun MessageDto.toDomainModel() = toLocalModel().toDomainModel()