package illyan.butler.services.ai.plugins.opentelemetry

import io.ktor.http.HttpMethod
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing

// setKnownMethods
fun KtorServerTracing.Configuration.knownMethods(vararg methods: HttpMethod) {
    knownMethods(methods.asIterable())
}

fun KtorServerTracing.Configuration.knownMethods(methods: Iterable<HttpMethod>) {
    setKnownMethods(methods.map { it.value }.toSet())
}