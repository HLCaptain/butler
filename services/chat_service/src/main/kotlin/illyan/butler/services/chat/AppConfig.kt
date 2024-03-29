package illyan.butler.services.chat

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
    data object Ktor {
        val DEVELOPMENT = System.getenv("KTOR_DEVELOPMENT").toBoolean()
        val PORT = System.getenv("KTOR_PORT")?.toIntOrNull() ?: 8080
        val DEBUG_CONTENT_TYPE = ContentType.Application.Json
        val BINARY_CONTENT_TYPE = ContentType.Application.ProtoBuf
        val DEFAULT_CONTENT_TYPE = if (DEVELOPMENT) DEBUG_CONTENT_TYPE else ContentType.parse(System.getenv("KTOR_DEFAULT_CONTENT_TYPE") ?: BINARY_CONTENT_TYPE.toString())
        val FALLBACK_CONTENT_TYPE = ContentType.parse(System.getenv("KTOR_FALLBACK_CONTENT_TYPE") ?: DEBUG_CONTENT_TYPE.toString())
        val SUPPORTED_CONTENT_TYPES =  listOf(
            DEFAULT_CONTENT_TYPE, // First is used as default
            FALLBACK_CONTENT_TYPE // Second is used as fallback
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
    data object Api {
        val IDENTITY_API_URL = System.getenv("IDENTITY_API_URL") ?: "http://localhost:8081"
        val NOTIFICATION_API_URL = System.getenv("NOTIFICATION_API_URL") ?: "http://localhost:8083"
    }
    data object Telemetry {
        val OTLP_EXPORTER_ENDPOINT = System.getenv("OTLP_EXPORTER_ENDPOINT") ?: "http://localhost:4317"
    }
    data object Database {
        val DATABASE_URL = System.getenv("DATABASE_URL") ?: "jdbc:postgresql://localhost:5432"
        val DATABASE_NAME = System.getenv("DATABASE_NAME") ?: "butler"
        val DATABASE_DRIVER = System.getenv("DATABASE_DRIVER") ?: "org.postgresql.Driver"
        val DATABASE_USER = System.getenv("DATABASE_USER") ?: "butler"
        val DATABASE_PASSWORD = System.getenv("DATABASE_PASSWORD") ?: "butler"
        val REDIS_URL = System.getenv("REDIS_URL") ?: "redis://localhost:6379"
        val REDIS_USER = System.getenv("REDIS_USER") ?: "butler"
        val REDIS_PASSWORD = System.getenv("REDIS_PASSWORD") ?: "butler"
    }
}