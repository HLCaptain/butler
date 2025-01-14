package illyan.butler.shared.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class PasswordResetRequest(val email: String)
