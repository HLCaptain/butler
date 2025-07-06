package illyan.butler.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler
import kotlinx.browser.window

@Composable
actual fun butlerUriHandler(): UriHandler? {
    return object : UriHandler {
        override fun openUri(uri: String) {
            window.open(uri, "_blank")
        }
    }
}
