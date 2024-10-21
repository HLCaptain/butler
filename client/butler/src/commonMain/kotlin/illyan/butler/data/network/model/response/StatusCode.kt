package illyan.butler.data.network.model.response

import kotlinx.serialization.Serializable

@Serializable
data class StatusCode(val code: Int, val message: String? = null) {

    // TODO: use codes in the future to show localized error messages
    companion object {
        // 404 Not Found
        val UserNotFound = StatusCode(4040, "User not found")
        val ChatNotFound = StatusCode(4041, "Chat not found")
        val ResourceNotFound = StatusCode(4042, "Resource not found")

        // 409 Conflict
        val UserAlreadyExists = StatusCode(4090, "User already exists")
    }
}