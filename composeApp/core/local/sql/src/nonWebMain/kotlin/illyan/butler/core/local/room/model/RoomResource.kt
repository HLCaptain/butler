package illyan.butler.core.local.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import illyan.butler.shared.model.chat.Source

@Entity(
    tableName = "resource",
)
data class RoomResource(
    @PrimaryKey
    val id: String,
    val createdAt: Long,
    val source: Source,
    val mimeType: String, // MIME mimeType
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomResource

        if (id != other.id) return false
        if (this@RoomResource.mimeType != other.mimeType) return false
        if (!data.contentEquals(other.data)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + mimeType.hashCode()
        result = 31 * result + data.contentHashCode()
        return result
    }
}
