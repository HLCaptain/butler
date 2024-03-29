package illyan.butler.api_gateway.data.utils

import illyan.butler.api_gateway.endpoints.utils.errorMessages

open class ApiException(val errorCodes: List<Int>) : Throwable(errorCodes.joinToString { errorMessages[it] ?: "" }) {
    constructor(errorCode: Int) : this(listOf(errorCode))
}

open class ApiGatewayException(errorCodes: List<Int>) : ApiException(errorCodes)
