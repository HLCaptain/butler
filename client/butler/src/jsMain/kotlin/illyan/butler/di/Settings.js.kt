package illyan.butler.di

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import illyan.butler.LocalStorageSlim
import org.koin.core.annotation.Single

@Single
actual fun provideSettings(): Settings {
    return StorageSettings(LocalStorageSlim.config.storage!!)
}