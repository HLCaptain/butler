package illyan.butler.data.local.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class RoomMessage(
    @PrimaryKey val id: String,
    val senderId: String,
    val message: String?,
    val time: Long,
    val chatId: String,
    val resourceIds: List<String>
)
