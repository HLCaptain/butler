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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Single
import org.koin.core.context.GlobalContext

@Single
actual fun provideSettings(): Settings {
    return provideEncryptedSettings(GlobalContext.get().get())
}

fun provideEncryptedSettings(context: Context): ObservableSettings {
    val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
    val sharedPreferences = EncryptedSharedPreferences.create(
        context,
        "secret_shared_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    return SharedPreferencesSettings(sharedPreferences)
}

@OptIn(ExperimentalSettingsApi::class)
@Single
actual fun provideFlowSettings(
    @NamedCoroutineDispatcherIO scope: CoroutineScope,
    @NamedCoroutineScopeIO dispatcher: CoroutineDispatcher
): FlowSettings {
    return provideEncryptedSettings(GlobalContext.get().get()).toFlowSettings(dispatcher)
}