package ai.nest.api_gateway.utils

import io.ktor.http.ContentType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf

data object AppConfig {
    /**
     * Deployment may be one of the following values:
     *  - development
     *  - staging
     *  - production
     */
    val DEPLOYMENT_ENVIRONMENT = System.getenv("DEPLOYMENT_ENVIRONMENT") ?: "development"
    data object Ktor {
        val DEVELOPMENT = System.getenv("KTOR_DEVELOPMENT").toBoolean()
        val PORT = System.getenv("KTOR_PORT")?.toIntOrNull() ?: 8080
        val DEBUG_CONTENT_TYPE = ContentType.Application.Json
        val DEFAULT_CONTENT_TYPE = if (DEVELOPMENT) DEBUG_CONTENT_TYPE else ContentType.parse(System.getenv("KTOR_DEFAULT_CONTENT_TYPE") ?: DEBUG_CONTENT_TYPE.toString())
        val FALLBACK_CONTENT_TYPE = ContentType.parse(System.getenv("KTOR_FALLBACK_CONTENT_TYPE") ?: DEBUG_CONTENT_TYPE.toString())
        val SUPPORTED_CONTENT_TYPES =  listOf(
            DEFAULT_CONTENT_TYPE, // First is used as default
            FALLBACK_CONTENT_TYPE // Second is used as fallback
        ).distinct()
        @OptIn(ExperimentalSerializationApi::class)
        val SERIALIZATION_FORMAT = SUPPORTED_CONTENT_TYPES.first().let { contentType ->
            when (contentType) {
                ContentType.Application.Json -> Json
                ContentType.Application.ProtoBuf -> ProtoBuf
                else -> Json
            }
        }
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
    data object Telemetry {
        val OTLP_EXPORTER_ENDPOINT = System.getenv("OTLP_EXPORTER_ENDPOINT") ?: "http://localhost:4317"
    }
}