package illyan.butler.core.local.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import illyan.butler.shared.model.chat.FilterOption
import illyan.butler.shared.model.chat.PromptConfiguration
import kotlin.uuid.ExperimentalUuidApi

@OptIn(ExperimentalUuidApi::class)
@Entity(tableName = "users")
data class RoomUser(
    @PrimaryKey
    val id: String,
    val endpoint: String, // e.g. "https://api.example.com/"
    val email: String,
    val username: String?,
    val displayName: String?,
    val phone: String?,
    val fullName: String?,
    val photoUrl: String?,
    val address: RoomAddress?,
    val filterOptions: Set<FilterOption>,
    val customPrompts: List<PromptConfiguration>,
)
