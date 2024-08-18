package illyan.butler.backend.data.model.response

import illyan.butler.backend.endpoints.utils.StatusCode
import kotlinx.serialization.Serializable

@Serializable
data class ServerErrorResponse(
    val statusCodes: List<StatusCode>
)
