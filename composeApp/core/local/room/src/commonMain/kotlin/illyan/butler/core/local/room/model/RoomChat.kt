package illyan.butler.core.local.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Capability
import illyan.butler.shared.model.chat.Source

@Entity(
    tableName = "chats",
)
data class RoomChat(
    @PrimaryKey
    val id: String,
    val createdAt: Long,
    val source: Source,
    val title: String? = null,
    val summary: String? = null,
    val lastUpdated: Long,
    val models: Map<Capability, AiSource> = emptyMap(),
)
