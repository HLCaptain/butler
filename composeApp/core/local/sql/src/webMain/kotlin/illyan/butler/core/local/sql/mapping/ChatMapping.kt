package illyan.butler.core.local.sql.mapping

import illyan.butler.core.local.sqldelight.Chats
import illyan.butler.domain.model.Chat
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Capability
import illyan.butler.shared.model.chat.Source
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun Chats.toDomainModel(): Chat {
    return Chat(
        id = Uuid.parse(id),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        source = Json.decodeFromString<Source>(source),
        title = title,
        summary = summary,
        lastUpdated = Instant.fromEpochMilliseconds(lastUpdated),
        models = Json.decodeFromString<Map<Capability, AiSource>>(models)
    )
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun Chat.toSqlDelightModel(): SqlDelightChat {
    return SqlDelightChat(
        id = id.toString(),
        createdAt = createdAt.toEpochMilliseconds(),
        source = Json.encodeToString(source),
        title = title,
        summary = summary,
        lastUpdated = lastUpdated.toEpochMilliseconds(),
        models = Json.encodeToString(models)
    )
}

data class SqlDelightChat(
    val id: String,
    val createdAt: Long,
    val source: String,
    val title: String?,
    val summary: String?,
    val lastUpdated: Long,
    val models: String
)
