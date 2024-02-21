package illyan.butler.api_gateway.plugins

import ai.nest.Butler_API_Gateway.BuildConfig
import ai.nest.api_gateway.utils.AppConfig
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.request.path
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.semconv.ResourceAttributes
import org.slf4j.event.Level

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }

    // Configure OpenTelemetry

    // Configure app resource
    val resource = Resource.builder()
        .put(ResourceAttributes.SERVICE_NAME, BuildConfig.PROJECT_NAME)
        .put(ResourceAttributes.SERVICE_NAMESPACE, BuildConfig.PROJECT_GROUP)
        .put(ResourceAttributes.SERVICE_VERSION, BuildConfig.PROJECT_VERSION)
        .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, AppConfig.DEPLOYMENT_ENVIRONMENT)
        .build()

    // Connecting to Jaeger
    val spanExporter = OtlpGrpcSpanExporter.builder()
        .setEndpoint(AppConfig.Telemetry.OTLP_EXPORTER_ENDPOINT)
        .build()
    val spanProcessor = BatchSpanProcessor.builder(spanExporter).build()
    val tracerProvider = SdkTracerProvider.builder()
        .setResource(Resource.getDefault().merge(resource))
        .addSpanProcessor(spanProcessor)
        .build()

    // Starting Prometheus server on localhost and default port 9464
    PrometheusHttpServer.builder().build()

    OpenTelemetrySdk.builder()
        .setTracerProvider(tracerProvider)
        .setPropagators(ContextPropagators.noop())
        .buildAndRegisterGlobal()

    install(KtorServerTracing) {
        setOpenTelemetry(GlobalOpenTelemetry.get())
    }
}