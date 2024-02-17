package illyan.butler

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import illyan.butler.util.log.initNapier
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule

fun main() = application {
    initNapier()
    startKoin { defaultModule() }
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}
