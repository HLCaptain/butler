package illyan.butler.core.local.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import illyan.butler.shared.model.chat.MessageStatus
import illyan.butler.shared.model.chat.SenderType
import illyan.butler.shared.model.chat.Source

@Entity(
    tableName = "messages",
)
data class RoomMessage(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val source: Source,
    val chatId: String,
    val sender: SenderType,
    val content: String? = null,
    val resourceIds: List<String> = emptyList(),
    val status: MessageStatus = MessageStatus.PENDING,
)
