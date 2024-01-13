package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.utils.ErrorHandler
import io.ktor.client.HttpClient
import io.ktor.util.Attributes
import org.koin.core.annotation.Single

@Single
class LocalizationService(
    private val client: HttpClient,
    private val attributes: Attributes,
    private val errorHandler: ErrorHandler
) {
}