package illyan.butler.shared.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyCredential(
    val providerUrl: String,
    val apiKey: String
)
