package illyan.butler.server.plugins.opentelemetry.client

import io.opentelemetry.instrumentation.ktor.v2_0.client.KtorClientTracingBuilder

// setEmitExperimentalHttpClientMetrics
fun KtorClientTracingBuilder.emitExperimentalHttpClientMetrics() {
    setEmitExperimentalHttpClientMetrics(true)
}