package illyan.butler.core.local.room.model

import androidx.room.Entity

@Entity(
    tableName = "api_key_credentials",
    primaryKeys = ["providerUrl"],
)
data class RoomApiKeyCredential(
    val name: String?,
    val providerUrl: String,
    val apiKey: String
)
