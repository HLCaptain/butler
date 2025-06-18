@file:OptIn(ExperimentalTime::class)

package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomToken
import illyan.butler.domain.model.Token
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun Token.toRoomModel() = RoomToken(
    token = token,
    tokenExpirationMillis = tokenExpiration.toEpochMilliseconds()
)

fun RoomToken.toDomainModel() = Token(
    token = token,
    tokenExpiration = Instant.fromEpochMilliseconds(tokenExpirationMillis)
)
