package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.localization.LabelDto
import ai.nest.api_gateway.data.model.localization.LocalizationPacketDto
import ai.nest.api_gateway.data.utils.ErrorHandler
import ai.nest.api_gateway.data.utils.tryToExecute
import ai.nest.api_gateway.utils.APIs
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.util.Attributes
import io.ktor.utils.io.InternalAPI
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.protobuf.ProtoBuf
import org.koin.core.annotation.Single

@Single
class LocalizationService(
    private val client: HttpClient,
    private val attributes: Attributes,
    private val errorHandler: ErrorHandler
) {
    @OptIn(ExperimentalSerializationApi::class, InternalAPI::class)
    suspend fun getLocalization(
        labels: List<LabelDto>,
        languageCode: String
    ) = client.tryToExecute<LocalizationPacketDto>(
        api = APIs.LOCALIZATION_API,
        attributes = attributes,
        setErrorMessage = { errorHandler.getLocalizedErrorMessage(it, languageCode) }
    ) {
        get("/localization") {
            val localization = LocalizationPacketDto(languageCode, labels)
            body = ProtoBuf.encodeToByteArray(LocalizationPacketDto.serializer(), localization)
        }
    }
}