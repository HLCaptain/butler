package illyan.butler.core.local.room.mapping

import illyan.butler.core.local.room.model.RoomApiKeyCredential
import illyan.butler.shared.model.auth.ApiKeyCredential

fun RoomApiKeyCredential.toDomainModel() = ApiKeyCredential(
    providerUrl = providerUrl,
    apiKey = apiKey,
)

fun ApiKeyCredential.toRoomModel() = RoomApiKeyCredential(
    providerUrl = providerUrl,
    apiKey = apiKey,
)
