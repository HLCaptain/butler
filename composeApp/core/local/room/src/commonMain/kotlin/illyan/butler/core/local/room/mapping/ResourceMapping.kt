@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomResource
import illyan.butler.domain.model.Resource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

fun RoomResource.toDomainModel() = Resource(
    id = Uuid.parse(id),
    mimeType = mimeType,
    data = data,
    createdAt = Instant.fromEpochMilliseconds(createdAt),
    source = source,
)

fun Resource.toRoomModel() = RoomResource(
    id = id.toString(),
    mimeType = mimeType,
    data = data,
    createdAt = createdAt.toEpochMilliseconds(),
    source = source,
)
