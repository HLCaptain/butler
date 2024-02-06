package ai.nest.api_gateway.data.utils

import ai.nest.api_gateway.data.service.LocalizationService
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single
import java.util.Locale

@Single
class ErrorHandler(
    private val localizationService: LocalizationService
) {
    suspend fun getLocalizedErrorMessage(errorCodes: List<String>, languageCode: String): Map<String, String> {
        return getLocalizedErrorMessage(errorCodes, Locale(languageCode))
    }
    suspend fun getLocalizedErrorMessage(errorCodes: List<String>, locale: Locale): Map<String, String> {
        return localizationService.getLocalizedMessages(errorCodes, locale).first().associate { it.key to it.value }
    }
}
