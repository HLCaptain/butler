package illyan.butler

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.russhwolf.settings.ExperimentalSettingsApi
import illyan.butler.core.local.settings.createSettings
import illyan.butler.utils.initNapier
import org.koin.core.context.startKoin
import org.koin.dsl.module

@OptIn(ExperimentalComposeUiApi::class, ExperimentalSettingsApi::class)
fun main() {
    initNapier()
    startKoin {
        modules(
            module {
                single { createSettings() }
            },
            *commonModules().toTypedArray()
        )
    }
    ComposeViewport("ComposeApp") {
        App()
    }
}
