package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomMessage
import illyan.butler.domain.model.Message
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
fun RoomMessage.toDomainModel() = Message(
    id = id,
    chatId = chatId,
    sender = sender,
    content = content,
    resourceIds = resourceIds,
    createdAt = timestamp,
    status = status,
)

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
fun Message.toRoomModel() = RoomMessage(
    id = id,
    chatId = chatId,
    sender = sender,
    content = content,
    resourceIds = resourceIds,
    timestamp = createdAt,
    status = status,
)
