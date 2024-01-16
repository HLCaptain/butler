package ai.nest.api_gateway.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.plugin.Koin

fun Application.configureDependencyInjection() {
    install(Koin) {
        modules(module { single { this@configureDependencyInjection } })
        defaultModule()
    }
}