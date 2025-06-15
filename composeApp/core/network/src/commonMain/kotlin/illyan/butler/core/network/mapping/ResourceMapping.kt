@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package illyan.butler.core.network.mapping

import illyan.butler.domain.model.Resource
import illyan.butler.shared.model.chat.ResourceDto
import illyan.butler.shared.model.chat.Source
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

fun ResourceDto.toDomainModel(source: Source) = Resource(
    id = id,
    mimeType = type,
    data = data,
    source = source,
    createdAt = createdAt
)

fun Resource.toNetworkModel() = ResourceDto(
    id = id,
    type = mimeType,
    data = data,
    createdAt = createdAt,
)
