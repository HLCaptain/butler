package illyan.butler.util.log

import io.github.aakira.napier.Antilog
import io.github.aakira.napier.LogLevel

// TODO: implement FirebaseCrashlytics logger
class CrashlyticsAntilog : Antilog() {
    override fun performLog(
        priority: LogLevel,
        tag: String?,
        throwable: Throwable?,
        message: String?
    ) {
        println("CrashlyticsAntilog: $priority: $tag: $message")
    }
}