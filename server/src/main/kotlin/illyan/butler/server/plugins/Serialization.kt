package illyan.butler.server.plugins

import illyan.butler.server.AppConfig
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.ExperimentalSerializationApi

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        // Order matters! First is the default serialization format
        AppConfig.Ktor.SUPPORTED_CONTENT_TYPES.forEach {
            println("Supported content type: $it")
            when (it) {
                ContentType.Application.Json -> json()
                ContentType.Application.ProtoBuf -> protobuf()
            }
        }
    }
}