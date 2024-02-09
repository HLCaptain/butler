package ai.nest.api_gateway.utils

enum class APIs(val key: String, val url: String) {
    IDENTITY_API("IDENTITY_API", AppConfig.Api.IDENTITY_API_URL),
    CHAT_API("CHAT_API", AppConfig.Api.CHAT_API_URL),
    NOTIFICATION_API("NOTIFICATION_API", AppConfig.Api.NOTIFICATION_API_URL),
    LOCALIZATION_API("LOCALIZATION_API", AppConfig.Api.LOCALIZATION_API_URL)
}