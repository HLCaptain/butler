package illyan.butler.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler
import io.github.aakira.napier.Napier
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

@Composable
actual fun butlerUriHandler(): UriHandler? {
    return object : UriHandler {
        override fun openUri(uri: String) {
            val url = NSURL(string = uri)
            if (UIApplication.sharedApplication.canOpenURL(url)) {
                UIApplication.sharedApplication.openURL(url)
            } else {
                Napier.e(tag = "UriHandler") { "Cannot open URL: $uri" }
            }
        }
    }
}
