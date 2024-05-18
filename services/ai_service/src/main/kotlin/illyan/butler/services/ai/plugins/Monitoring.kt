package illyan.butler.services.ai.plugins

import illyan.butler.services.ai.AppConfig
import illyan.butler.services.ai.BuildConfig
import illyan.butler.services.ai.plugins.opentelemetry.attributeExtractor
import illyan.butler.services.ai.plugins.opentelemetry.capturedRequestHeaders
import illyan.butler.services.ai.plugins.opentelemetry.capturedResponseHeaders
import illyan.butler.services.ai.plugins.opentelemetry.knownMethods
import illyan.butler.services.ai.plugins.opentelemetry.spanKindExtractor
import illyan.butler.services.ai.plugins.opentelemetry.spanStatusExtractor
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.metrics.micrometer.MicrometerMetrics
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.openapi.openAPI
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.core.instrument.binder.jvm.ClassLoaderMetrics
import io.micrometer.core.instrument.binder.jvm.JvmCompilationMetrics
import io.micrometer.core.instrument.binder.jvm.JvmGcMetrics
import io.micrometer.core.instrument.binder.jvm.JvmHeapPressureMetrics
import io.micrometer.core.instrument.binder.jvm.JvmMemoryMetrics
import io.micrometer.core.instrument.binder.jvm.JvmThreadMetrics
import io.micrometer.core.instrument.binder.system.FileDescriptorMetrics
import io.micrometer.core.instrument.binder.system.ProcessorMetrics
import io.micrometer.core.instrument.binder.system.UptimeMetrics
import io.micrometer.core.instrument.distribution.DistributionStatisticConfig
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry
import io.opentelemetry.api.common.Attributes
import io.opentelemetry.api.trace.SpanKind
import io.opentelemetry.api.trace.StatusCode
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer
import io.opentelemetry.instrumentation.ktor.v2_0.server.KtorServerTracing
import io.opentelemetry.instrumentation.resources.ContainerResource
import io.opentelemetry.instrumentation.resources.HostResource
import io.opentelemetry.instrumentation.resources.OsResource
import io.opentelemetry.instrumentation.resources.ProcessResource
import io.opentelemetry.instrumentation.resources.ProcessRuntimeResource
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import io.opentelemetry.sdk.logs.SdkLoggerProvider
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader
import io.opentelemetry.sdk.resources.Resource
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler
import io.opentelemetry.semconv.ResourceAttributes
import kotlinx.datetime.Clock
import org.slf4j.event.Level
import kotlin.time.Duration.Companion.seconds

fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
        callIdMdc(HttpHeaders.XRequestId)
    }
    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { callId: String ->
            callId.isNotEmpty()
        }
    }

    // Configure OpenTelemetry

    // Configure app resource
    val resource = Resource.getDefault()
        .merge(ContainerResource.get())
        .merge(HostResource.get())
        .merge(OsResource.get())
        .merge(ProcessResource.get())
        .merge(ProcessRuntimeResource.get())
        .merge(Resource.create(
            Attributes.builder()
                .put(ResourceAttributes.SERVICE_NAME, BuildConfig.PROJECT_NAME)
                .put(ResourceAttributes.SERVICE_NAMESPACE, BuildConfig.PROJECT_GROUP)
                .put(ResourceAttributes.SERVICE_VERSION, BuildConfig.PROJECT_VERSION)
                .put(ResourceAttributes.DEPLOYMENT_ENVIRONMENT, AppConfig.DEPLOYMENT_ENVIRONMENT)
                .build()))

    // Connecting to Jaeger
    val spanExporter = OtlpGrpcSpanExporter.builder()
        .setEndpoint(AppConfig.Telemetry.OTEL_EXPORTER_OTLP_ENDPOINT)
        .build()
    val spanProcessor = BatchSpanProcessor.builder(spanExporter).build()
    val tracerProvider = SdkTracerProvider.builder()
        .setResource(Resource.getDefault().merge(resource))
        .setSampler(if (AppConfig.Ktor.DEVELOPMENT) Sampler.alwaysOn() else Sampler.alwaysOff())
        .addSpanProcessor(spanProcessor)
        .build()
    val meterProvider = SdkMeterProvider.builder()
        .registerMetricReader(PrometheusHttpServer.builder().build()) // Starting Prometheus server on localhost and default port 9464
        .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
        .setResource(Resource.getDefault().merge(resource))
        .build()
    val loggerProvider = SdkLoggerProvider.builder()
        .setResource(Resource.getDefault().merge(resource))
        .build()

    val otlp = OpenTelemetrySdk.builder()
        .setMeterProvider(meterProvider)
        .setLoggerProvider(loggerProvider)
        .setTracerProvider(tracerProvider)
        .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
        .buildAndRegisterGlobal()

//    val otlp = AutoConfiguredOpenTelemetrySdk.initialize().openTelemetrySdk

    install(KtorServerTracing) {
        setOpenTelemetry(otlp)

        knownMethods(HttpMethod.DefaultMethods)
        capturedRequestHeaders(HttpHeaders.UserAgent, HttpHeaders.XRequestId)
        capturedResponseHeaders(HttpHeaders.ContentType, HttpHeaders.XRequestId)

        spanStatusExtractor {
            val path = response?.call?.request?.path() ?: ""
            if (path.contains("/span-status-extractor") || error != null) {
                spanStatusBuilder.setStatus(StatusCode.ERROR)
            }
        }

        spanKindExtractor {
            if (httpMethod == HttpMethod.Post) {
                SpanKind.PRODUCER
            } else {
                SpanKind.CLIENT
            }
        }

        attributeExtractor {
            onStart {
                attributes.put("start-time", Clock.System.now().toEpochMilliseconds())
            }
            onEnd {
                attributes.put("end-time", Clock.System.now().toEpochMilliseconds())
            }
        }
    }
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        distributionStatisticConfig = DistributionStatisticConfig.Builder()
            .percentilesHistogram(true)
            .maximumExpectedValue(20.seconds.inWholeNanoseconds.toDouble())
            .serviceLevelObjectives(
                100.seconds.inWholeMilliseconds.toDouble(),
                500.seconds.inWholeMilliseconds.toDouble()
            ).build()
        meterBinders = listOf(
            JvmMemoryMetrics(),
            JvmGcMetrics(),
            ProcessorMetrics(),
            JvmThreadMetrics(),
            ClassLoaderMetrics(),
            UptimeMetrics(),
            JvmCompilationMetrics(),
            FileDescriptorMetrics(),
            JvmHeapPressureMetrics(),
        )
    }
    routing {
        get("/metrics") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }
    routing {
        swaggerUI(path = "openapi")
        openAPI(path = "openapi")
    }
    Napier.base(DebugAntilog())
}
