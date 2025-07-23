package illyan.butler.core.local.sql.mapping

import illyan.butler.core.local.sqldelight.Resources
import illyan.butler.domain.model.Resource
import kotlinx.serialization.json.Json
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun Resources.toDomainModel(): Resource {
    return Resource(
        id = Uuid.parse(id),
        createdAt = Instant.fromEpochMilliseconds(createdAt),
        mimeType = mimeType,
        data = data_,
        source = Json.decodeFromString(source)
    )
}

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
fun Resource.toSqlDelightModel(): SqlDelightResource {
    return SqlDelightResource(
        id = id.toString(),
        createdAt = createdAt.toEpochMilliseconds(),
        mimeType = Json.encodeToString(mimeType),
        source = Json.encodeToString(source),
        data = data
    )
}

data class SqlDelightResource(
    val id: String,
    val createdAt: Long,
    val source: String,
    val mimeType: String,
    val data: ByteArray,
)
