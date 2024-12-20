package illyan.butler.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ApiKeyCredential(
    val name: String?,
    val providerUrl: String,
    val apiKey: String
)
