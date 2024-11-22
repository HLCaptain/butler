package illyan.butler.core.local.room.model

import androidx.room.Entity

@Entity(
    tableName = "chats",
    primaryKeys = ["id"],
)
data class RoomChat(
    val id: String,
    val created: Long,
    val name: String?,
    val members: List<String>,
    val aiEndpoints: Map<String, String>,
    val summary: String?,
)
