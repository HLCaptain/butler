package ai.nest.api_gateway.plugins

import io.ktor.serialization.kotlinx.json.json
import io.ktor.serialization.kotlinx.protobuf.protobuf
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.protobuf.ProtoBuf

@OptIn(ExperimentalSerializationApi::class)
fun Application.configureSerialization() {
    install(ContentNegotiation) {
        protobuf()
        json()
    }
    routing {
        get("/protobuf/kotlinx-serialization") {
            call.respond(ProtoBuf.encodeToByteArray(mapOf("hello" to "world")))
        }
        get("/json/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}