package illyan.butler.data.firebase

import dev.gitlive.firebase.FirebaseOptions
import illyan.butler.config.BuildConfig

fun getDesktopFirebaseOptions() = getBaseFirebaseOptions(BuildConfig.FIREBASE_DESKTOP_APP_ID)
fun getWebFirebaseOptions() = getBaseFirebaseOptions(BuildConfig.FIREBASE_WEB_APP_ID)

private fun getBaseFirebaseOptions(appId: String) = FirebaseOptions(
    applicationId = appId,
    apiKey = BuildConfig.FIREBASE_WEB_AND_DESKTOP_API_KEY,
    projectId = BuildConfig.FIREBASE_PROJECT_ID,
    storageBucket = BuildConfig.FIREBASE_STORAGE_BUCKET,
    gcmSenderId = BuildConfig.FIREBASE_MESSAGING_SENDER_ID,
    authDomain = BuildConfig.FIREBASE_AUTH_DOMAIN,
)
