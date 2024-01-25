package ai.nest.api_gateway.data.service

import ai.nest.api_gateway.data.model.localization.LabelDto
import ai.nest.api_gateway.data.model.localization.LanguageLocalizationDto
import ai.nest.api_gateway.data.utils.tryToExecuteWebSocket
import ai.nest.api_gateway.utils.APIs
import io.ktor.client.HttpClient
import io.ktor.util.Attributes
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.annotation.Single

@Single
class LocalizationService(
    private val client: HttpClient,
    private val attributes: Attributes,
) {
    private val cachedLocalizedMessages: MutableMap<Locale, StateFlow<LanguageLocalizationDto?>> = mutableMapOf()

    private fun getLocalizedMessages(
        locale: Locale
    ): Flow<LanguageLocalizationDto?> {
        if (cachedLocalizedMessages.containsKey(locale)) {
            return cachedLocalizedMessages[locale]!!
        }

        try {
            val localizationFlow = getLocalization(locale)
            cachedLocalizedMessages[locale] = localizationFlow
        } catch (e: Exception) {
            // TODO: log error with OpenTelemetry or Napier
            val defaultLocalizationFlow = getDefaultLocalization()
            cachedLocalizedMessages[locale] = defaultLocalizationFlow
        }

        return cachedLocalizedMessages[locale]!!
    }

    fun getLocalizedMessages(
        keys: List<String>,
        locale: Locale
    ): Flow<List<LabelDto>> {
        val localizedMessages = getLocalizedMessages(locale)
        return localizedMessages.filterNotNull().map { languageLocalizationDto ->
            languageLocalizationDto.labels.filter { labelDto ->
                keys.contains(labelDto.key)
            }
        }
    }

    suspend fun getLocalizedMessage(key: String, locale: Locale) = getLocalizedMessages(listOf(key), locale).first().first().value

    private fun getLocalization(
        locale: Locale
    ) = client.tryToExecuteWebSocket<LanguageLocalizationDto>(
        api = APIs.LOCALIZATION_API,
        attributes = attributes,
        path = "/localization/$locale"
    )

    private fun getDefaultLocalization() = client.tryToExecuteWebSocket<LanguageLocalizationDto>(
        api = APIs.LOCALIZATION_API,
        attributes = attributes,
        path = "/localization"
    )
}