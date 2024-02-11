import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import illyan.butler.App
import illyan.butler.data.firebase.getWebFirebaseOptions
import illyan.butler.util.log.initNapier
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initNapier()
    startKoin { defaultModule() }

    Firebase.initialize(
        context = null,
        options = getWebFirebaseOptions()
    )

    onWasmReady {
        CanvasBasedWindow("Butler App") {
            App()
        }
    }
}