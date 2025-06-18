package illyan.butler.domain.model

import illyan.butler.shared.model.chat.MessageStatus
import illyan.butler.shared.model.chat.SenderType
import illyan.butler.shared.model.chat.Source
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Immutable domain model representing a single chat message.
 */
@OptIn(ExperimentalUuidApi::class)
data class Message @OptIn(ExperimentalTime::class, ExperimentalUuidApi::class) constructor(
    override val id: Uuid = Uuid.random(),
    override val createdAt: Instant = Clock.System.now(),
    override val source: Source,
    val chatId: Uuid,
    val sender: SenderType,
    val content: String? = null,
    val resourceIds: List<Uuid> = emptyList(),
    val status: MessageStatus = MessageStatus.PENDING,
) : Entity
