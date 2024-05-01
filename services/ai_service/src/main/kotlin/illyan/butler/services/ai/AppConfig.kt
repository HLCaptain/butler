package illyan.butler.services.ai

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
        val DEVELOPMENT = System.getenv("DEVELOPMENT")?.toBooleanStrictOrNull() ?: true
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
        val SSL_CERT_PASSWORD = System.getenv("SSL_CERT_PASSWORD") ?: "password"
    }
    data object Api {
        val CHAT_API_URL = System.getenv("CHAT_API_URL") ?: "http://localhost:8084"
        val LOCAL_AI_OPEN_AI_API_URL = System.getenv("LOCAL_AI_API_URL") ?: "http://localai:8080"
        val ANYSCALE_API_URL = System.getenv("ANYSCALE_API_URL") ?: "https://api.endpoints.anyscale.com/v1"
        val OPEN_AI_API_URL = System.getenv("OPEN_AI_API_URL") ?: "https://api.openai.com/v1"
        // val OTHER_AI_PROVIDER_OPEN_AI_API_URL...
        val OPEN_AI_API_KEY = System.getenv("OPEN_AI_API_KEY") ?: "sk-1234567890abcdef1234567890abcdef"
        val ANYSCALE_API_CREDENTIAL = System.getenv("ANYSCALE_API_CREDENTIAL") ?: "sk-1234567890abcdef1234567890abcdef"
        val OPEN_AI_API_URLS_AND_KEYS = mapOf(
            LOCAL_AI_OPEN_AI_API_URL to "",
            ANYSCALE_API_URL to ANYSCALE_API_CREDENTIAL,
            OPEN_AI_API_URL to OPEN_AI_API_KEY
        )
    }
}