package illyan.butler.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

/**
 * No IO dispatcher in Kotlin Coroutines Core, provide platform specific implementation
 */
@Single
@Named(KoinNames.DispatcherIO)
expect fun provideDispatcherIO(): CoroutineDispatcher

@Single
@Named(KoinNames.DispatcherMain)
fun provideDispatcherMain() = Dispatchers.Main

@Single
@Named(KoinNames.DispatcherDefault)
fun provideDispatcherDefault() = Dispatchers.Default
