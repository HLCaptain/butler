package illyan.butler.core.local.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import illyan.butler.domain.model.MessageStatus
import illyan.butler.domain.model.SenderType
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "messages",

)
data class RoomMessage @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class) constructor(
    @PrimaryKey val id: Uuid,
    val chatId: Uuid,
    val sender: SenderType,
    val content: String? = null,
    val resourceIds: List<String> = emptyList(),
    val timestamp: Instant = Clock.System.now(),
    val status: MessageStatus = MessageStatus.PENDING
)
