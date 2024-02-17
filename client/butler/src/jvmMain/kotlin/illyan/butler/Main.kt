package illyan.butler

import android.content.Context
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.firebase.FirebasePlatform
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.initialize
import illyan.butler.data.firebase.getDesktopFirebaseOptions
import illyan.butler.util.log.initNapier
import io.github.aakira.napier.Napier
import org.koin.core.context.startKoin
import org.koin.ksp.generated.defaultModule

fun main() = application {
    initNapier()
    startKoin { defaultModule() }
    initFirebase()
    Window(onCloseRequest = ::exitApplication) {
        App()
    }
}

fun initFirebase() {
    initFirebasePlatform()
    Firebase.initialize(
        context = Context(),
        options = getDesktopFirebaseOptions()
    )
    initFirebaseFirestore()
}

fun initFirebasePlatform() {
    // TODO: implement Firebase local cache storage, like a SQLite table
    FirebasePlatform.initializeFirebasePlatform(object : FirebasePlatform() {
        val storage = mutableMapOf<String, String>()
        override fun store(key: String, value: String) = storage.set(key, value)
        override fun retrieve(key: String) = storage[key]
        override fun clear(key: String) { storage.remove(key) }
        override fun log(msg: String) = Napier.d(msg)
    })
}

fun initFirebaseFirestore() {
    val settings = FirebaseFirestoreSettings.Builder()
        .setPersistenceEnabled(false)
//        .setSslEnabled(false)
        .build()
    FirebaseFirestore.getInstance().firestoreSettings = settings
}
