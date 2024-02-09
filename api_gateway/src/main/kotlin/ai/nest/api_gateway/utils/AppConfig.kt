package ai.nest.api_gateway.utils

data object AppConfig {
    data object Ktor {
        val DEVELOPMENT = System.getenv("KTOR_DEVELOPMENT").toBoolean()
        val PORT = System.getenv("PORT")?.toIntOrNull() ?: 8080
        val DEFAULT_CONTENT_TYPE = System.getenv("KTOR_DEFAULT_CONTENT_TYPE") ?: "application/json"
    }
    data object Jwt {
        val SECRET = System.getenv("JWT_SECRET") ?: "your_jwt_secret"
        val ISSUER = System.getenv("JWT_ISSUER") ?: "your_jwt_issuer"
        val AUDIENCE = System.getenv("JWT_AUDIENCE") ?: "your_jwt_audience"
        val REALM = System.getenv("JWT_REALM") ?: "your_jwt_realm"
    }
    data object Api {
        val IDENTITY_API_URL = System.getenv("IDENTITY_API_URL") ?: "http://localhost:8081"
        val CHAT_API_URL = System.getenv("CHAT_API_URL") ?: "http://localhost:8082"
        val NOTIFICATION_API_URL = System.getenv("NOTIFICATION_API_URL") ?: "http://localhost:8083"
        val LOCALIZATION_API_URL = System.getenv("LOCALIZATION_API_URL") ?: "http://localhost:8084"
    }
}