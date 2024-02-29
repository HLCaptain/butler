package illyan.butler.data.mapping

import illyan.butler.data.network.model.ChatDto
import illyan.butler.db.Chat
import illyan.butler.domain.model.DomainChat
import illyan.butler.util.log.randomUUID

fun Chat.toDomainModel() = DomainChat(
    uuid = uuid,
    name = name,
    userUUID = userUUID,
    modelUUID = modelUUID
)

fun DomainChat.toNetworkModel() = ChatDto(
    id = uuid,
    name = name,
    userUUID = userUUID,
    modelUUID = modelUUID
)

fun ChatDto.toLocalModel() = Chat(
    uuid = id ?: randomUUID(),
    name = name,
    userUUID = userUUID,
    modelUUID = modelUUID
)

fun Chat.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainChat.toLocalModel() = toNetworkModel().toLocalModel()
fun ChatDto.toDomainModel() = toLocalModel().toDomainModel()