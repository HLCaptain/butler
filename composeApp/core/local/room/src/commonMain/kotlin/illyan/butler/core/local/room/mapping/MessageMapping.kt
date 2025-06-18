package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomMessage
import illyan.butler.domain.model.Message
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
fun RoomMessage.toDomainModel() = Message(
    id = Uuid.parse(id),
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    source = source,
    chatId = Uuid.parse(chatId),
    sender = sender,
    content = content,
    resourceIds = resourceIds.map { Uuid.parse(it) },
    status = status,
)

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
fun Message.toRoomModel() = RoomMessage(
    id = id.toString(),
    createdAt = createdAt.toEpochMilliseconds(),
    source = source,
    chatId = chatId.toString(),
    sender = sender,
    content = content,
    resourceIds = resourceIds.map { it.toString() },
    status = status,
)
