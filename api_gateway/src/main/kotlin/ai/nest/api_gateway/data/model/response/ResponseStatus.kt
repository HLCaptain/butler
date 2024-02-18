package ai.nest.api_gateway.data.model.response

import kotlinx.serialization.Serializable

@Serializable
data class ResponseStatus(
    val errorCodes: List<Int>? = null, // map of error codes and messages
    val httpStatusCode: Int? // http status code
)