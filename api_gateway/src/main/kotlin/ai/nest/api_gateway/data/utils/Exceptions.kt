package ai.nest.api_gateway.data.utils

open class ApiGatewayException(message: String) : Throwable(message)

class LocalizedMessageException(errorMessages: Map<Int, String>) : ApiGatewayException(errorMessages.toString())
