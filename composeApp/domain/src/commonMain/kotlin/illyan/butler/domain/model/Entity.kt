package illyan.butler.domain.model

import illyan.butler.shared.model.chat.Source
import kotlin.time.ExperimentalTime
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)
sealed interface Entity {
    val id: Uuid
    val source: Source
    val createdAt: Instant

    val deviceOnly: Boolean
        get() = source !is Source.Server
}
