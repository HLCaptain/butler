package illyan.butler.data.network.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(val token: String)
