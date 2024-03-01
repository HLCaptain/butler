package illyan.butler.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.FlowSettings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.Single

@Single
expect fun provideSettings(): Settings

@OptIn(ExperimentalSettingsApi::class)
@Single
expect fun provideFlowSettings(
    @NamedCoroutineDispatcherIO scope: CoroutineScope,
    @NamedCoroutineScopeIO dispatcher: CoroutineDispatcher
): FlowSettings