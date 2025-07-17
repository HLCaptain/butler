package illyan.butler

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.russhwolf.settings.ExperimentalSettingsApi
import illyan.butler.core.local.datastore.getDataStore
import illyan.butler.core.local.settings.createSettings
import illyan.butler.generated.resources.Res
import illyan.butler.generated.resources.app_name
import illyan.butler.generated.resources.butler_logo
import illyan.butler.utils.initNapier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.startKoin
import org.koin.dsl.module

@OptIn(ExperimentalSettingsApi::class)
fun main() = application {
    initNapier()
    startKoin {
        modules(
            module {
                single { getDataStore() }
                single { createSettings() }
            },
            *commonModules().toTypedArray()
        )
    }
    Window(
        onCloseRequest = ::exitApplication,
        title = stringResource(Res.string.app_name),
        icon = painterResource(Res.drawable.butler_logo)
    ) {
        App()
    }
}
