package ai.nest.api_gateway.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseStatus(
    val errorMessages: Map<Int, String>? = null, // map of error codes and messages
    val successMessage: String? = null,
    val httpStatusCode: Int? // http status code
)