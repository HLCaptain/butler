package ai.nest.api_gateway.data.model.response

import ai.nest.api_gateway.data.model.localization.LabelDto
import kotlinx.serialization.Serializable

@Serializable
data class ResponseStatus(
    val errorMessages: List<LabelDto>? = null, // map of error codes and messages
    val successMessage: String? = null,
    val httpStatusCode: Int? // http status code
)