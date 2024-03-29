package illyan.butler.data.mapping

import illyan.butler.db.ErrorEvent
import illyan.butler.domain.model.DomainErrorEvent

fun ErrorEvent.toDomainModel() = DomainErrorEvent(
    id = id,
    platform = platform,
    message = message,
    stackTrace = stackTrace,
    metadata = metadata,
    os = os,
    timestamp = timestamp,
    state = state
)

fun DomainErrorEvent.toLocalModel() = ErrorEvent(
    id = id,
    platform = platform,
    message = message,
    stackTrace = stackTrace,
    metadata = metadata,
    os = os,
    timestamp = timestamp,
    state = state
)
