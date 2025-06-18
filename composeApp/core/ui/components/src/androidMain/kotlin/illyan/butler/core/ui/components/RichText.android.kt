package illyan.butler.core.ui.components

import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import io.github.aakira.napier.Napier

@Composable
actual fun butlerUriHandler(): UriHandler? {
    val context = LocalContext.current
    return object : UriHandler {
        override fun openUri(uri: String) {
            when {
                uri.startsWith("mailto:") -> {
                    try {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SENDTO
                            data = uri.toUri()
                            putExtra(Intent.EXTRA_EMAIL, uri.removePrefix("mailto:"))
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    } catch (e: Exception) {
                        Log.e("EmailSpan", e.message, e)
                    }
                }
                uri.endsWith(".pdf") || uri.endsWith("/download") || uri.endsWith(".pdfsig") -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            setDataAndType(uri.toUri(), "application/pdf")
                            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        val packageManager: PackageManager = context.packageManager
                        val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                        if (activities.isNotEmpty()) {
                            context.startActivity(Intent.createChooser(intent, "Open with ..."))
                        } else {
                            Toast.makeText(context, "Please download a pdf reader application to open this document", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Napier.e("PdfLinkSpan", e)
                    }
                }
                else -> {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, uri.toUri()).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        Napier.e("LinkSpan", e)
                    }
                }
            }
        }
    }
}