package illyan.butler.core.local.sqldelight.mapping

import illyan.butler.core.local.sqldelight.Messages
import illyan.butler.domain.model.Message
import illyan.butler.shared.model.chat.MessageStatus
import illyan.butler.shared.model.chat.SenderType
import illyan.butler.shared.model.chat.Source
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun Messages.toDomainModel(): Message {
    return Message(
        id = Uuid.parse(id),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        source = Json.decodeFromString<Source>(source),
        chatId = Uuid.parse(chatId),
        sender = Json.decodeFromString<SenderType>(sender),
        content = content,
        resourceIds = Json.decodeFromString<List<String>>(resourceIds).map { Uuid.parse(it) },
        status = Json.decodeFromString<MessageStatus>(status)
    )
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun Message.toSqlDelightModel(): SqlDelightMessage {
    return SqlDelightMessage(
        id = id.toString(),
        createdAt = createdAt.toEpochMilliseconds(),
        source = Json.encodeToString(source),
        chatId = chatId.toString(),
        sender = Json.encodeToString(sender),
        content = content,
        resourceIds = Json.encodeToString(resourceIds.map { it.toString() }),
        status = Json.encodeToString(status)
    )
}

data class SqlDelightMessage(
    val id: String,
    val createdAt: Long,
    val source: String,
    val chatId: String,
    val sender: String,
    val content: String?,
    val resourceIds: String,
    val status: String
)
