package illyan.butler.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single

/**
 * No IO dispatcher on JS, so we use the default dispatcher
 */
@Single
@Named(KoinNames.DispatcherIO)
actual fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.Default
