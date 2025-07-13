package illyan.butler.core.local.sql.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_key_credentials")
data class RoomApiKeyCredential(
    @PrimaryKey
    val providerUrl: String,
    val apiKey: String
)
