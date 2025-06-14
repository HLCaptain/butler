@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomResource
import illyan.butler.domain.model.Resource
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

fun RoomResource.toDomainModel() = Resource(
    id = id,
    mimeType = mimeType,
    data = data,
    createdAt = createdAt,
    source = source,
)

fun Resource.toRoomModel() = RoomResource(
    id = id,
    mimeType = mimeType,
    data = data,
    createdAt = createdAt,
    source = source,
)
