package illyan.butler.shared.model.response

import kotlinx.serialization.Serializable

@Serializable
data class StatusCode(
    val code: Int, // Custom code for more accurate error handling
    val message: String? = null // Optional message to be sent to the client
) {

    companion object {
        // 404 Not Found
        val UserNotFound = StatusCode(4040, "User not found")
        val ChatNotFound = StatusCode(4041, "Chat not found")
        val ResourceNotFound = StatusCode(4042, "Resource not found")

        // 409 Conflict
        val UserAlreadyExists = StatusCode(4090, "User already exists")
    }
}