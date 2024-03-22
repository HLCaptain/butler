package illyan.butler.api_gateway.data.model.response

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable

@Serializable
data class ServerResponse<T>(
    val value: T?,
    val isSuccess: Boolean = true,
    val status: ResponseStatus
) {

    companion object {
        fun error(errorCodes: List<Int>?, httpStatusCode: HttpStatusCode): ServerResponse<String> {
            return ServerResponse(
                value = null,
                isSuccess = false,
                status = ResponseStatus(errorCodes = errorCodes, httpStatusCode = httpStatusCode.value)
            )
        }

        inline fun <reified T> success(result: T): ServerResponse<T> {
            return ServerResponse(
                value = result,
                isSuccess = true,
                status = ResponseStatus(httpStatusCode = HttpStatusCode.OK.value),
            )
        }
    }
}