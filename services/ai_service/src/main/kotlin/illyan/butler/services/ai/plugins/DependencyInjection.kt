package illyan.butler.services.ai.plugins

import illyan.butler.services.ai.data.service.LlmService
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ksp.generated.defaultModule
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin

fun Application.configureDependencyInjection() {
    install(Koin) {
        modules(module { single { this@configureDependencyInjection } })
        defaultModule()
    }
    getKoin().get<LlmService>().loadModels()
}