package illyan.butler.data.mapping

import illyan.butler.data.network.model.chat.ChatDto
import illyan.butler.db.Chat
import illyan.butler.domain.model.DomainChat

fun Chat.toDomainModel() = DomainChat(
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

fun ChatDto.toLocalModel() = Chat(
    id = id!!,
    created = created!!,
    name = name,
    members = members,
    aiEndpoints = aiEndpoints,
    summary = summary
)

fun Chat.toNetworkModel() = toDomainModel().toNetworkModel()
fun DomainChat.toLocalModel() = toNetworkModel().toLocalModel()
fun ChatDto.toDomainModel() = toLocalModel().toDomainModel()