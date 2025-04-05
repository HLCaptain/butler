package illyan.butler.core.network.mapping

import illyan.butler.domain.model.DomainMessage
import illyan.butler.shared.model.chat.MessageDto

fun MessageDto.toDomainModel() = DomainMessage(
    id = id,
    senderId = senderId,
    messageContent = message,
    resourceIds = resourceIds,
    time = time,
    chatId = chatId
)

fun DomainMessage.toNetworkModel() = MessageDto(
    id = id,
    senderId = senderId,
    message = messageContent,
    resourceIds = resourceIds,
    time = time,
    chatId = chatId
)
