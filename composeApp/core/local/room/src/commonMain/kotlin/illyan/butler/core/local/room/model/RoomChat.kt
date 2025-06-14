package illyan.butler.core.local.room.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import illyan.butler.domain.model.AiSource
import illyan.butler.domain.model.Capability
import illyan.butler.domain.model.Source
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(
    tableName = "chats",
)
data class RoomChat @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class) constructor(
    @PrimaryKey
    val id: Uuid = Uuid.random(),
    val createdAt: Instant = Clock.System.now(),
    val source: Source,
    val title: String? = null,
    val summary: String? = null,
    val lastUpdated: Instant = Clock.System.now(),
    val models: Map<Capability, AiSource> = emptyMap(),
    val ownerId: Uuid,
)
