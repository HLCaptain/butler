package illyan.butler.data.room.model

import androidx.room.Entity

@Entity(
    tableName = "resource",
    primaryKeys = ["id"]
)
data class RoomResource(
    val id: String,
    val mimeType: String,
    val data: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RoomResource

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
}
