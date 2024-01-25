package ai.nest.api_gateway.data.utils

import ai.nest.api_gateway.data.model.localization.LabelDto

open class ApiGatewayException(message: String) : Throwable(message)

class LocalizedMessageException(val errorMessages: List<LabelDto>) : ApiGatewayException(errorMessages.toString())
