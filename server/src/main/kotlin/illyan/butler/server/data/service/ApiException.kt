package illyan.butler.server.data.service

import illyan.butler.shared.model.response.StatusCode
import io.ktor.http.HttpStatusCode

open class ApiException(
    val statusCodes: List<StatusCode>,
    httpStatusCode: HttpStatusCode? = null
) : Throwable(statusCodes.joinToString { "${it.code} code: ${it.message}" }) {

    val httpStatusCode: HttpStatusCode = httpStatusCode ?: statusCodes.firstOrNull()?.let {
        // Take first 3 letters of custom status code to "convert" it to HTTP status code
        HttpStatusCode.fromValue(it.code.toString().take(3).toInt())
    } ?: HttpStatusCode.InternalServerError

    constructor(
        statusCode: StatusCode,
        httpStatusCode: HttpStatusCode? = null
    ) : this(listOf(statusCode), httpStatusCode)
}