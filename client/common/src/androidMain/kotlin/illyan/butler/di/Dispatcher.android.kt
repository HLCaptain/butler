package illyan.butler.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.koin.core.annotation.Single

@Single
@NamedCoroutineDispatcherMain
actual fun provideDispatcherMain(): CoroutineDispatcher = Dispatchers.Main

@Single
@NamedCoroutineDispatcherIO
actual fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.IO