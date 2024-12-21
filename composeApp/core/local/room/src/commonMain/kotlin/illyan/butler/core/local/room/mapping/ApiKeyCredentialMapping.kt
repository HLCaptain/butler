package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomApiKeyCredential
import illyan.butler.domain.model.ApiKeyCredential

fun RoomApiKeyCredential.toDomainModel() = ApiKeyCredential(
    providerUrl = providerUrl,
    apiKey = apiKey,
    name = name
)

fun ApiKeyCredential.toRoomModel() = RoomApiKeyCredential(
    providerUrl = providerUrl,
    apiKey = apiKey,
    name = name
)
