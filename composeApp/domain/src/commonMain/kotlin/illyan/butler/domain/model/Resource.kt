package illyan.butler.domain.model

import illyan.butler.shared.model.chat.Source
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
data class Resource(
    override val id: Uuid = Uuid.random(),
    override val createdAt: Instant = Clock.System.now(),
    override val source: Source,
    val mimeType: String, // MIME mimeType
    val data: ByteArray
) : Entity {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Resource

        if (id != other.id) return false
        if (mimeType != other.mimeType) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "Resource(id=$id, mimeType='$mimeType', data.size=${data.size})"
    }
}
