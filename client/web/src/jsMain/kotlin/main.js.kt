import androidx.compose.ui.window.Window
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import illyan.butler.App
import illyan.butler.data.firebase.getWebFirebaseOptions
import illyan.butler.util.log.initNapier
import org.jetbrains.skiko.wasm.onWasmReady
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule

fun main() {
    initNapier()
    startKoin { defaultModule() }

    Firebase.initialize(
        context = null,
        options = getWebFirebaseOptions()
    )

    onWasmReady {
        Window("demo") {
            App()
        }
    }
}