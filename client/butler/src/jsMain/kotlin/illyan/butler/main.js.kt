import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import illyan.butler.App
import illyan.butler.util.log.initNapier
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initNapier()
    startKoin { defaultModule() }

    onWasmReady {
        CanvasBasedWindow("Butler App") {
            App()
        }
    }
}