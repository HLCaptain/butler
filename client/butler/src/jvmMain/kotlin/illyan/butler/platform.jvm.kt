package illyan.butler

import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import illyan.butler.config.BuildConfig
import illyan.butler.utils.audio.AudioRecorder
import illyan.butler.utils.sound.JvmAudioRecorder

actual fun getPlatformName(): String {
    return "JVM"
}

actual fun isDebugBuild() = BuildConfig.DEBUG

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun getWindowSizeInDp(): Pair<Dp, Dp> {
    val size = LocalWindowInfo.current.containerSize
    val density = LocalDensity.current.density
    return size.height.dp / density to size.width.dp / density
}

actual fun getOsName(): String {
    return System.getProperty("os.name")
}

actual fun getSystemMetadata(): Map<String, String> {
    return mapOf(
        "os.arch" to System.getProperty("os.arch"),
        "os.version" to System.getProperty("os.version"),
        "java.version" to System.getProperty("java.version"),
        "java.vendor" to System.getProperty("java.vendor"),
        "java.vm.version" to System.getProperty("java.vm.version"),
        "java.vm.vendor" to System.getProperty("java.vm.vendor"),
        "java.vm.name" to System.getProperty("java.vm.name")
    )
}

actual fun getAudioRecorder(): AudioRecorder? {
    return JvmAudioRecorder()
}
