@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package illyan.butler.core.network.mapping

import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.ChatDto
import illyan.butler.shared.model.chat.Source
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi

fun ChatDto.toDomainModel(source: Source) = Chat(
    id = id,
    createdAt = createdAt,
    title = name,
    source = source,
    models = models,
    summary = summary,
)

fun Chat.toNetworkModel() = ChatDto(
    id = id,
    createdAt = createdAt,
    name = title,
    ownerId = when (source) {
        is Source.Device -> (source as Source.Device).deviceId
        is Source.Server -> (source as Source.Server).userId
    },
    summary = summary,
    models = models
)
