package illyan.butler.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.UriHandler
import io.github.aakira.napier.Napier
import java.awt.Desktop
import java.net.URI

@Composable
actual fun butlerUriHandler(): UriHandler? {
    return object : UriHandler {
        override fun openUri(uri: String) {
            // Open URI in the default browser
            if (Desktop.isDesktopSupported()) {
                try {
                    val desktop = Desktop.getDesktop()
                    desktop.browse(URI.create(uri))
                } catch (e: Exception) {
                    // Handle the exception if the desktop action fails
                    Napier.e("Failed to open URI: $uri", e)
                }
            } else {
                // Fallback for environments where Desktop is not supported
                Napier.e("Desktop is not supported, cannot open URI: $uri")
            }
        }
    }
}
