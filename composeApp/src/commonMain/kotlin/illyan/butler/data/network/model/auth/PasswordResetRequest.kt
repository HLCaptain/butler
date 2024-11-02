package illyan.butler.data.network.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(val email: String)
