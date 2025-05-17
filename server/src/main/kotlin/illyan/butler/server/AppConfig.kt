package illyan.butler.server

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
        val DEVELOPMENT = System.getenv("DEVELOPMENT").toBoolean()
        val PORT = System.getenv("KTOR_PORT")?.toIntOrNull() ?: 8080
        val DEBUG_CONTENT_TYPE = ContentType.Application.Json
        val BINARY_CONTENT_TYPE = ContentType.Application.ProtoBuf
        val DEFAULT_CONTENT_TYPE = if (DEVELOPMENT) DEBUG_CONTENT_TYPE else ContentType.parse(System.getenv("KTOR_DEFAULT_CONTENT_TYPE") ?: BINARY_CONTENT_TYPE.toString())
        val FALLBACK_CONTENT_TYPE = ContentType.parse(System.getenv("KTOR_FALLBACK_CONTENT_TYPE") ?: DEBUG_CONTENT_TYPE.toString())
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
        val SERIALIZATION_FORMAT = SERIALIZATION_FORMATS.first()
    }
    data object Jwt {
        val SECRET = System.getenv("JWT_SECRET") ?: "your_jwt_secret"
        val ISSUER = System.getenv("JWT_ISSUER") ?: "your_jwt_issuer"
        val AUDIENCE = System.getenv("JWT_AUDIENCE") ?: "your_jwt_audience"
        val REALM = System.getenv("JWT_REALM") ?: "your_jwt_realm"
    }
    data object Api {
        val LOCAL_AI_OPEN_AI_API_URL = System.getenv("LOCAL_AI_API_URL") ?: "http://localai:8080"
        val OPEN_AI_API_URL = System.getenv("OPEN_AI_API_URL") ?: "https://api.openai.com/v1/"
        // val OTHER_AI_PROVIDER_OPEN_AI_API_URL...
        val OPEN_AI_API_KEY = System.getenv("OPEN_AI_API_KEY") ?: "sk-1234567890abcdef1234567890abcdef"
        val OPEN_AI_API_URLS_AND_KEYS = mapOf(
            LOCAL_AI_OPEN_AI_API_URL to "",
            OPEN_AI_API_URL to OPEN_AI_API_KEY
        )
    }
    data object Telemetry {
        val OTEL_EXPORTER_OTLP_ENDPOINT = System.getenv("OTEL_EXPORTER_OTLP_ENDPOINT") ?: "http://localhost:4317"
    }
    data object Database {
        val DATABASE_USER = System.getenv("DATABASE_USER") ?: "butler"
        val DATABASE_PASSWORD = System.getenv("DATABASE_PASSWORD") ?: "butler"
        val DATABASE_URL = System.getenv("DATABASE_URL") ?: "r2dbc:postgresql:${DATABASE_USER}:${DATABASE_PASSWORD}@localhost:5432/postgres?lc_messages=en_US.UTF-8"
        val DATABASE_NAME = System.getenv("DATABASE_NAME") ?: "butler"
        val DATABASE_DRIVER = System.getenv("DATABASE_DRIVER") ?: "org.postgresql.Driver"
    }
}
