package illyan.butler.di

import com.microsoft.credentialstorage.StorageProvider
import com.microsoft.credentialstorage.model.StoredCredential
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import illyan.butler.EncryptedPreferences
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Single
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Single
actual fun provideSettings(): Settings {
    return provideEncryptedPreferencesSettings()
}

@OptIn(ExperimentalEncodingApi::class)
fun provideEncryptedPreferencesSettings(): PreferencesSettings {
    val credentialStorage = StorageProvider.getCredentialStorage(true, StorageProvider.SecureOption.REQUIRED)

    // If master key is not found, create one
    val storedMasterKey = credentialStorage.get("ButlerMasterKey")?.password
    val masterKey = if (storedMasterKey == null) {
        val keyGenerator = KeyGenerator.getInstance("AES")
        val secretKey = keyGenerator.generateKey()
        val keyChars = secretKey.encoded.toString().toCharArray()
        credentialStorage.add("ButlerMasterKey", StoredCredential("ButlerChatApp", keyChars))
        secretKey
    } else {
        val decodedKey = Base64.decode(storedMasterKey.contentToString())
        SecretKeySpec(decodedKey, 0, decodedKey.size, "AES")
    }

    // Encrypt the preferences
    return PreferencesSettings(EncryptedPreferences(masterKey))
}

@OptIn(ExperimentalSettingsApi::class)
@Single
actual fun provideFlowSettings(
    @NamedCoroutineDispatcherIO scope: CoroutineScope,
    @NamedCoroutineScopeIO dispatcher: CoroutineDispatcher
): FlowSettings {
//    val dataStore = PreferenceDataStoreFactory.createWithPath(scope = scope) { "preferences_datastore.json".toPath() }
//    return DataStoreSettings(dataStore)
    return provideEncryptedPreferencesSettings().toFlowSettings(dispatcher)
}
