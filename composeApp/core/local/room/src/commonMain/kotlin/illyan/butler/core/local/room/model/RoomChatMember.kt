package illyan.butler.core.local.room.model

import androidx.room.Entity

@Entity(
    tableName = "chat_members",
    primaryKeys = ["chatId", "userId"],
)
data class RoomChatMember(
    val chatId: String,
    val userId: String,
)
