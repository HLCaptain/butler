package illyan.butler.backend.data.service

import illyan.butler.backend.endpoints.utils.StatusCode

open class ApiException(val statusCodes: List<StatusCode>) : Throwable(statusCodes.joinToString { "${it.code} code: ${it.message}" }) {
    constructor(statusCode: StatusCode) : this(listOf(statusCode))
}