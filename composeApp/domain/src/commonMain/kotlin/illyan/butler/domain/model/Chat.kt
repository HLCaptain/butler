package illyan.butler.domain.model

import illyan.butler.shared.model.chat.AiSource
import illyan.butler.shared.model.chat.Capability
import illyan.butler.shared.model.chat.Source
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class Chat @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class) constructor(
    override val id: Uuid = Uuid.random(),
    override val createdAt: Instant = Clock.System.now(),
    override val source: Source,
    val title: String? = null,
    val summary: String? = null,
    val lastUpdated: Instant = Clock.System.now(),
    val models: Map<Capability, AiSource> = emptyMap(),
) : Entity
