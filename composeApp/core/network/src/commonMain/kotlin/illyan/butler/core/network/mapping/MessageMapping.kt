@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package illyan.butler.core.network.mapping

import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.MessageDto
import illyan.butler.shared.model.chat.Source
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

fun MessageDto.toDomainModel(source: Source) = Message(
    id = id,
    createdAt = Instant.fromEpochMilliseconds(time!!),
    source = source,
    chatId = chatId,
    sender = sender,
    content = content,
    resourceIds = resourceIds,
    status = status,
)

fun Message.toNetworkModel() = MessageDto(
    id = id,
    sender = sender,
    content = content,
    resourceIds = resourceIds,
    time = createdAt.toEpochMilliseconds(),
    chatId = chatId,
    status = status,
)
