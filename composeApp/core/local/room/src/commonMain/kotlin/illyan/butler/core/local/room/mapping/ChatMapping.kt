@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomChat
import illyan.butler.domain.model.Chat
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

fun RoomChat.toDomainModel() = Chat(
    id = id,
    title = title,
    summary = summary,
    createdAt = createdAt,
    lastUpdated = lastUpdated,
    models = models,
    ownerId = ownerId,
    source = source,
)

fun Chat.toRoomModel() = RoomChat(
    id = id,
    title = title,
    summary = summary,
    createdAt = createdAt,
    lastUpdated = lastUpdated,
    models = models,
    ownerId = ownerId,
    source = source,
)
