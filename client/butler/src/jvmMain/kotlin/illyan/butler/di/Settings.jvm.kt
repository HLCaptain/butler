package illyan.butler.di

import com.microsoft.credentialstorage.StorageProvider
import com.microsoft.credentialstorage.model.StoredCredential
import com.russhwolf.settings.PreferencesSettings
import com.russhwolf.settings.Settings
import illyan.butler.EncryptedPreferences
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import org.koin.core.annotation.Single
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
@Single
actual fun provideSettings(): Settings {
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