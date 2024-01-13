package ai.nest.api_gateway.data.utils

import org.koin.core.annotation.Single

@Single
class LocalizedMessagesFactory {
    companion object {
        val defaultLocalizedMessages = EnglishLocalizedMessages()
    }

    fun createLocalizedMessages(languageCode: String): LocalizedMessages {
        return map[languageCode.uppercase()] ?: EnglishLocalizedMessages()
    }

    private val map = mapOf(
        "EN" to EnglishLocalizedMessages()
    )
}