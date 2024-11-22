package illyan.butler.shared.model.response

import kotlinx.serialization.Serializable

// Just a custom Error code (Int)
@Serializable
data class ServerErrorResponse(
    val statusCodes: List<StatusCode>
)
