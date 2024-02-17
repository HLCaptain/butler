package illyan.butler.data.mapping

import illyan.butler.data.network.model.MessageDto
import illyan.butler.db.Message
import illyan.butler.domain.model.DomainMessage

fun Message.toDomainModel() = DomainMessage(
    uuid = uuid,
    senderUUID = senderUUID,
    role = role,
    message = message,
    timestamp = timestamp,
    chatUUID = chatUUID
)

fun DomainMessage.toNetworkModel() = MessageDto(
    uuid = uuid,
    senderUUID = senderUUID,
    role = role,
    message = message,
    timestamp = timestamp,
    chatUUID = chatUUID
)

fun MessageDto.toLocalModel() = Message(
    uuid = uuid,
    senderUUID = senderUUID,
    role = role,
    message = message,
    timestamp = timestamp,
    chatUUID = chatUUID
)

fun Message.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainMessage.toLocalModel() = toNetworkModel().toLocalModel()
fun MessageDto.toDomainModel() = toLocalModel().toDomainModel()