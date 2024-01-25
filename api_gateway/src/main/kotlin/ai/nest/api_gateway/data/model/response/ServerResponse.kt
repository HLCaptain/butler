package ai.nest.api_gateway.data.model.response

import ai.nest.api_gateway.data.model.localization.LabelDto
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse<T>(
    val value: T?,
    val isSuccess: Boolean = true,
    val status: ResponseStatus
) {

    companion object {
        fun error(errorMessages: List<LabelDto>?, httpStatusCode: HttpStatusCode): ServerResponse<String> {
            return ServerResponse(
                value = null,
                isSuccess = false,
                status = ResponseStatus(errorMessages = errorMessages, httpStatusCode = httpStatusCode.value)
            )
        }

        inline fun <reified T> success(result: T, successMessage: String?): ServerResponse<T> {
            return ServerResponse(
                value = result,
                isSuccess = true,
                status = ResponseStatus(successMessage = successMessage, httpStatusCode = HttpStatusCode.OK.value),
            )
        }
    }
}