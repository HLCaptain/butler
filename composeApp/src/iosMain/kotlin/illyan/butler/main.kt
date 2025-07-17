package illyan.butler

import androidx.compose.ui.uikit.OnFocusBehavior
import androidx.compose.ui.window.ComposeUIViewController
import com.russhwolf.settings.ExperimentalSettingsApi
import illyan.butler.core.local.datastore.getDataStore
import illyan.butler.core.local.settings.createSettings
import illyan.butler.utils.initNapier
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.UIKit.UIViewController

@OptIn(ExperimentalSettingsApi::class)
@Suppress("unused") // Called from Swift
fun MainViewController(): UIViewController = ComposeUIViewController(
    configure = { onFocusBehavior = OnFocusBehavior.DoNothing },
) {
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
    App()
}
