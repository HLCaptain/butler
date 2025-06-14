package illyan.butler.shared.model.chat

import illyan.butler.shared.model.serializers.InstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
@Serializable
data class ResourceDto(
    val id: Uuid = Uuid.random(),
    val type: String,
    val data: ByteArray,
    @Serializable(InstantSerializer::class)
    val createdAt: Instant = Clock.System.now(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResourceDto

        if (id != other.id) return false
        if (type != other.type) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + type.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }

    override fun toString(): String {
        return "ResourceDto(id=$id, type='$type', data.size=${data.size})"
    }
}
