package illyan.butler.api_gateway.data.model.chat

import kotlinx.serialization.Serializable

@Serializable
data class ResourceDto(
    val id: String? = null,
    val type: String,
    val data: ByteArray
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
}
