package illyan.butler.core.local.room.model

import androidx.room.Entity
import illyan.butler.domain.model.Capability
import illyan.butler.domain.model.ModelConfig

@Entity(
    tableName = "chats",
    primaryKeys = ["id"],
)
data class RoomChat(
    val id: String,
    val created: Long? = null,
    val name: String? = null,
    val ownerId: String,
    val models: Map<Capability, ModelConfig>,
    val summary: String? = null
)
