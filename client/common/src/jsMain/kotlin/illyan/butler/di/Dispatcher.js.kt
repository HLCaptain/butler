package illyan.butler.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single

@Single
@NamedCoroutineDispatcherMain
actual fun provideDispatcherMain(): CoroutineDispatcher = Dispatchers.Main

/**
 * No IO dispatcher on JS, so we use the default dispatcher
 */
@Single
@NamedCoroutineDispatcherIO
actual fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.Default
