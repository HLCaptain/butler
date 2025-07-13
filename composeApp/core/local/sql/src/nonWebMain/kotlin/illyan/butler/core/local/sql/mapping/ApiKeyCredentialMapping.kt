package illyan.butler.core.local.sql.mapping

import illyan.butler.core.local.sql.model.RoomApiKeyCredential
import illyan.butler.shared.model.auth.ApiKeyCredential

fun RoomApiKeyCredential.toDomainModel() = ApiKeyCredential(
    providerUrl = providerUrl,
    apiKey = apiKey,
)

fun ApiKeyCredential.toRoomModel() = RoomApiKeyCredential(
    providerUrl = providerUrl,
    apiKey = apiKey,
)
