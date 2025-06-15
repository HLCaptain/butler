@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomChat
import illyan.butler.domain.model.Chat
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun RoomChat.toDomainModel() = Chat(
    id = Uuid.parse(id),
    title = title,
    summary = summary,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    lastUpdated =  Instant.fromEpochMilliseconds(lastUpdated),
    models = models,
    source = source,
)

fun Chat.toRoomModel() = RoomChat(
    id = id.toString(),
    title = title,
    summary = summary,
    createdAt = createdAt.toEpochMilliseconds(),
    lastUpdated = lastUpdated.toEpochMilliseconds(),
    models = models,
    source = source,
)
