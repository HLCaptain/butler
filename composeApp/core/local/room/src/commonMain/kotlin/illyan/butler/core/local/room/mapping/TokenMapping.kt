package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomToken
import illyan.butler.domain.model.DomainToken

fun DomainToken.toRoomModel() = RoomToken(
    token = token,
    tokenExpirationMillis = tokenExpirationMillis
)

fun RoomToken.toDomainModel() = DomainToken(
    token = token,
    tokenExpirationMillis = tokenExpirationMillis
)
