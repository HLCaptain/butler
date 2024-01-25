package ai.nest.api_gateway.data.model.response

import ai.nest.api_gateway.data.model.localization.LabelDto
import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse<T>(
    val value: T?,
    val isSuccess: Boolean = true,
    val status: ResponseStatus
) {

    companion object {
        fun error(errorMessages: List<LabelDto>?, httpStatusCode: Int): ServerResponse<String> {
            return ServerResponse(
                value = null,
                isSuccess = false,
                status = ResponseStatus(errorMessages = errorMessages, httpStatusCode = httpStatusCode)
            )
        }

        inline fun <reified T> success(result: T, successMessage: String?): ServerResponse<T> {
            return ServerResponse(
                value = result,
                isSuccess = true,
                status = ResponseStatus(successMessage = successMessage, httpStatusCode = 200),
            )
        }
    }
}