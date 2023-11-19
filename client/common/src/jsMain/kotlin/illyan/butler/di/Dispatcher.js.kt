package illyan.butler.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Factory

@Factory
@NamedCoroutineDispatcherMain
actual fun provideDispatcherMain(): CoroutineDispatcher = Dispatchers.Main

/**
 * No IO dispatcher on JS, so we use the default dispatcher
 */
@Factory
@NamedCoroutineDispatcherIO
actual fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.Default
