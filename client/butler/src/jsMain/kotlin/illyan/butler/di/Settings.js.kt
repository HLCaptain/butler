package illyan.butler.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import illyan.butler.LocalStorageSlim
import illyan.butler.ObservableStorageSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Single

@Single
actual fun provideSettings(): Settings {
    return StorageSettings(LocalStorageSlim.config.storage!!)
}

@OptIn(ExperimentalSettingsApi::class)
@Single
actual fun provideFlowSettings(
    @NamedCoroutineDispatcherIO scope: CoroutineScope,
    @NamedCoroutineScopeIO dispatcher: CoroutineDispatcher
): FlowSettings {
    return ObservableStorageSettings(provideSettings()).toFlowSettings(dispatcher)
}
