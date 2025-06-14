package illyan.butler.domain.model

import illyan.butler.shared.model.serializers.InstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Serializable
data class Token(
    val token: String,
    @Serializable(with = InstantSerializer::class)
    val tokenExpiration: Instant
)
