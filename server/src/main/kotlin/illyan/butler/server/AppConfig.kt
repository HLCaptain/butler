package illyan.butler.server

import com.typesafe.config.ConfigFactory
import io.ktor.http.ContentType
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf

// add config loader
private val config = ConfigFactory.systemEnvironment().withFallback(ConfigFactory.load())

data object AppConfig {
    data object Ktor {
        /**
         * Deployment may be one of the following values:
         *  - development
         *  - staging
         *  - production
         */
        val ENVIRONMENT: String = config.getString("KTOR_ENVIRONMENT")
        val DEVELOPMENT = config.getBoolean("KTOR_DEVELOPMENT")
        val PORT = config.getInt("KTOR_PORT")
        val DEBUG_CONTENT_TYPE = ContentType.Application.Json
        val BINARY_CONTENT_TYPE = ContentType.Application.ProtoBuf
        val DEFAULT_CONTENT_TYPE = if (DEVELOPMENT) DEBUG_CONTENT_TYPE else ContentType.parse(config.getString("KTOR_DEFAULT_CONTENT_TYPE"))
        val FALLBACK_CONTENT_TYPE = ContentType.parse(config.getString("KTOR_FALLBACK_CONTENT_TYPE"))
        val SUPPORTED_CONTENT_TYPES =  listOf(
            DEFAULT_CONTENT_TYPE, // First is used as default
            FALLBACK_CONTENT_TYPE, // Second is used as fallback
            BINARY_CONTENT_TYPE // Supporting binary either way
        ).distinct()
        @OptIn(ExperimentalSerializationApi::class)
        val SERIALIZATION_FORMATS = SUPPORTED_CONTENT_TYPES.map { contentType ->
            when (contentType) {
                ContentType.Application.Json -> Json
                ContentType.Application.ProtoBuf -> ProtoBuf
                else -> Json
            }
        }.distinct()
        val DEFAULT_SERIALIZATION_FORMAT = SERIALIZATION_FORMATS.first()
    }
    data object Jwt {
        val SECRET = config.getString("JWT_SECRET")
        val ISSUER = config.getString("JWT_ISSUER")
        val AUDIENCE = config.getString("JWT_AUDIENCE")
        val REALM = config.getString("JWT_REALM")
    }
    data object Api {
        // val OTHER_AI_PROVIDER_OPEN_AI_API_URL...
        val OPEN_AI_API_URLS_AND_KEYS = Json.decodeFromString<Map<String, String>>(config.getString("OPEN_AI_API_URLS_AND_KEYS"))
    }
    data object Telemetry {
        val OTEL_EXPORTER_OTLP_ENDPOINT = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") ?: "http://localhost:4317"
    }
    data object Database {
        val USER = config.getString("DATABASE_USER")
        val PASSWORD = config.getString("DATABASE_PASSWORD")
        val NAME = config.getString("DATABASE_NAME")
        val DRIVER = config.getString("DATABASE_DRIVER")
        val HOST = config.getString("DATABASE_HOST")
        val PORT = config.getInt("DATABASE_PORT")
    }
}
