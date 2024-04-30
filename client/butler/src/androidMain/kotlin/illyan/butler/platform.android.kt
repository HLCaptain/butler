package illyan.butler

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import illyan.butler.utils.AudioRecorder
import illyan.butler.utils.sound.AndroidAudioRecorder
import org.koin.core.context.GlobalContext

actual fun getPlatformName(): String {
    return "Android"
}

actual fun isDebugBuild(): Boolean = BuildConfig.DEBUG

@Composable
actual fun getWindowSizeInDp(): Pair<Dp, Dp> {
    val size = LocalConfiguration.current
    return size.screenHeightDp.dp to size.screenWidthDp.dp
}

actual fun getOsName(): String {
    return Build.VERSION.SDK_INT.toString()
}


actual fun getSystemMetadata(): Map<String, String> {
    return mapOf(
        "Build.VERSION.SDK_INT" to Build.VERSION.SDK_INT.toString(),
        "Build.VERSION.RELEASE" to Build.VERSION.RELEASE,
        "Build.VERSION.CODENAME" to Build.VERSION.CODENAME,
        "Build.VERSION.INCREMENTAL" to Build.VERSION.INCREMENTAL,
        "Build.VERSION.BASE_OS" to Build.VERSION.BASE_OS,
        "Build.VERSION.PREVIEW_SDK_INT" to Build.VERSION.PREVIEW_SDK_INT.toString(),
        "Build.VERSION.SECURITY_PATCH" to Build.VERSION.SECURITY_PATCH
    )
}

actual fun getAudioRecorder(): AudioRecorder? {
    return AndroidAudioRecorder(GlobalContext.get().get())
}
