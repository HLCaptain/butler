package illyan.butler.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single
import org.koin.core.context.GlobalContext

@Single
actual fun provideSettings(): Settings {
    return provideEncryptedSettings(GlobalContext.get().get())
}

fun provideEncryptedSettings(context: Context): ObservableSettings {
    Napier.v("Creating encrypted settings")
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    Napier.v("Master key created")
    val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    Napier.v("Encrypted shared preferences created")
    return SharedPreferencesSettings(sharedPreferences)
}

@OptIn(ExperimentalSettingsApi::class)
@Single
actual fun provideFlowSettings(
    @Named(KoinNames.CoroutineScopeIO) scope: CoroutineScope,
    @Named(KoinNames.DispatcherIO) dispatcher: CoroutineDispatcher
): FlowSettings {
    return provideEncryptedSettings(GlobalContext.get().get()).toFlowSettings(dispatcher)
//    val context: Context = GlobalContext.get().get()
//    return DataStoreSettings(context.dataStore)
}

//val Context.dataStore by preferencesDataStore(name = "settings")
